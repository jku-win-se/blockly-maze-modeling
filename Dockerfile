# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy local libs and install them into local Maven repo
COPY libs libs
RUN set -eux; \
    mvn install:install-file -Dfile=libs/at.ac.tuwien.big.momot.core_2.0.0.202604151118.jar -DgroupId=at.ac.tuwien.big.momot -DartifactId=at.ac.tuwien.big.momot.core -Dversion=2.0.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=libs/at.ac.tuwien.big.moea_2.0.0.202604151118.jar -DgroupId=at.ac.tuwien.big.momot -DartifactId=at.ac.tuwien.big.moea -Dversion=2.0.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=libs/org.eclipse.emf.henshin.model_1.8.0.202302121604.jar -DgroupId=org.eclipse.emf -DartifactId=org.eclipse.emf.henshin.model -Dversion=1.8.0 -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=libs/org.eclipse.emf.henshin.interpreter_1.8.0.202302121604.jar -DgroupId=org.eclipse.emf -DartifactId=org.eclipse.emf.henshin.interpreter -Dversion=1.8.0 -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=libs/org.eclipse.ocl.ecore_3.23.0.v20260217-0639.jar -DgroupId=org.eclipse.ocl -DartifactId=org.eclipse.ocl.ecore -Dversion=3.23.0 -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=libs/org.eclipse.ocl.common_1.23.0.v20260217-0639.jar -DgroupId=org.eclipse.ocl -DartifactId=org.eclipse.ocl.common -Dversion=1.23.0 -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=libs/org.eclipse.ocl_3.23.0.v20260217-0639.jar -DgroupId=org.eclipse.ocl -DartifactId=org.eclipse.ocl -Dversion=3.23.0 -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=libs/org.eclipse.xtext.xbase.lib_2.42.0.v20260223-0608.jar -DgroupId=org.eclipse.xtext -DartifactId=org.eclipse.xtext.xbase.lib -Dversion=2.42.0 -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=libs/org.eclipse.core.runtime_3.34.200.v20251220-0953.jar -DgroupId=org.eclipse.core -DartifactId=runtime -Dversion=3.34.200 -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=libs/org.eclipse.equinox.common_3.20.300.v20251111-0312.jar -DgroupId=org.eclipse.equinox -DartifactId=common -Dversion=3.20.300 -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=libs/lpg.runtime.java_2.0.17.v201004271640.jar -DgroupId=lpg.runtime -DartifactId=java -Dversion=2.0.17 -Dpackaging=jar -DgeneratePom=true; \
    curl -fsSL "https://repo1.maven.org/maven2/org/openjdk/nashorn/nashorn-core/15.4/nashorn-core-15.4.jar" -o nashorn.jar; \
    curl -fsSL "https://repo1.maven.org/maven2/org/ow2/asm/asm/7.3.1/asm-7.3.1.jar" -o asm.jar; \
    curl -fsSL "https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/7.3.1/asm-commons-7.3.1.jar" -o asm-commons.jar; \
    curl -fsSL "https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/7.3.1/asm-tree-7.3.1.jar" -o asm-tree.jar; \
    curl -fsSL "https://repo1.maven.org/maven2/org/ow2/asm/asm-util/7.3.1/asm-util-7.3.1.jar" -o asm-util.jar; \
    curl -fsSL "https://repo1.maven.org/maven2/org/ow2/asm/asm-analysis/7.3.1/asm-analysis-7.3.1.jar" -o asm-analysis.jar; \
    mvn install:install-file -Dfile=nashorn.jar -DgroupId=org.openjdk.nashorn -DartifactId=nashorn-core -Dversion=15.4 -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=asm.jar -DgroupId=org.ow2.asm -DartifactId=asm -Dversion=7.3.1 -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=asm-commons.jar -DgroupId=org.ow2.asm -DartifactId=asm-commons -Dversion=7.3.1 -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=asm-tree.jar -DgroupId=org.ow2.asm -DartifactId=asm-tree -Dversion=7.3.1 -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=asm-util.jar -DgroupId=org.ow2.asm -DartifactId=asm-util -Dversion=7.3.1 -Dpackaging=jar -DgeneratePom=true; \
    mvn install:install-file -Dfile=asm-analysis.jar -DgroupId=org.ow2.asm -DartifactId=asm-analysis -Dversion=7.3.1 -Dpackaging=jar -DgeneratePom=true;

# Copy poms and download dependencies
COPY pom.xml .
COPY blocky_model/pom.xml blocky_model/
COPY blocky_game/pom.xml blocky_game/
COPY blocky_momot/pom.xml blocky_momot/
RUN mvn dependency:go-offline -B

# Copy source and build
COPY blocky_model blocky_model
COPY blocky_game blocky_game
COPY blocky_momot blocky_momot
COPY entrypoint.sh .
RUN mvn clean install -DskipTests

# Stage 2: Runtime stage
FROM maven:3.9.6-eclipse-temurin-21
WORKDIR /app

# Install JavaFX native dependencies and web GUI tools (noVNC)
RUN apt-get update && apt-get install -y \
    libgtk-3-0 \
    libgl1-mesa-dri \
    libgl1-mesa-glx \
    libx11-6 \
    libxext6 \
    libxi6 \
    libxrender1 \
    libxtst6 \
    libxkbcommon0 \
    libasound2 \
    libfontconfig1 \
    xvfb \
    x11vnc \
    novnc \
    websockify \
    && rm -rf /var/lib/apt/lists/*

# Copy the built application from the build stage
COPY --from=build /app /app
COPY --from=build /root/.m2 /root/.m2

# Set the working directory to the app root
WORKDIR /app

# Expose noVNC port
EXPOSE 6080

# Use the entrypoint script to launch virtual display and app
ENTRYPOINT ["./entrypoint.sh"]
