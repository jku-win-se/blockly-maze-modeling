package blocky.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HenshinNsUriRewriter {

	private static final String DEFAULT_HENSHIN_PATH =
			"transformations/statement_insertions_henshin_text.henshin";

	private static final String FROM_PREFIX = "../model/blocky.ecore#";
	private static final String TO_PREFIX = "http://www.example.org/blocky#";
	private static final String IMPORT_HREF = "http://www.example.org/blocky#/";

	public static void main(String[] args) throws IOException {
		final String henshinPathString = (args.length >= 1 && !args[0].isBlank())
				? args[0]
				: DEFAULT_HENSHIN_PATH;

		final Path henshinPath = Path.of(henshinPathString);

		if (!Files.exists(henshinPath)) {
			throw new IllegalStateException("File not found: " + henshinPath.toAbsolutePath());
		}

		String content = Files.readString(henshinPath, StandardCharsets.UTF_8);

		// 1) Rewrite all Ecore href prefixes to nsURI-based hrefs
		content = content.replace(FROM_PREFIX, TO_PREFIX);

		// 2) Force module import to nsURI (keep trailing '/')
		content = content.replaceAll(
				"<imports\\s+href=\"[^\"]*\"\\s*/>",
				"<imports href=\"" + IMPORT_HREF + "\"/>"
		);

		Files.writeString(henshinPath, content, StandardCharsets.UTF_8);

		// Verify rewrite worked
		final String after = Files.readString(henshinPath, StandardCharsets.UTF_8);

		if (after.contains(FROM_PREFIX)) {
			throw new IllegalStateException("nsURI rewrite incomplete: still found '" + FROM_PREFIX + "' in " + henshinPath);
		}
		if (!after.contains("<imports href=\"" + IMPORT_HREF + "\"/>")) {
			throw new IllegalStateException("Import href not set to '" + IMPORT_HREF + "' in " + henshinPath);
		}

		System.out.println("OK: rewritten nsURI hrefs in " + henshinPath);
	}

	private HenshinNsUriRewriter() {
		// utility class
	}
}