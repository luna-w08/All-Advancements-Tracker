"""Export generated biome artwork as native 16x16 UI sprites (requires Pillow).

Usage: python tools/export_biome_sprites.py build/biome-sprite-generation
Each input JSON records a biome, generated source PNG, and the original prompt.
"""

import json
from pathlib import Path
import sys

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
DESTINATION = ROOT / "src/main/resources/assets/aaigttracker/biome_sprites"


def export(source_directory: Path) -> None:
    provenance = []
    for manifest in sorted(source_directory.glob("*.json")):
        entry = json.loads(manifest.read_text(encoding="utf-8-sig"))
        with Image.open(entry["source"]) as source:
            source = source.convert("RGBA")
            bounds = source.getchannel("A").point(lambda a: 255 if a >= 128 else 0).getbbox()
            if bounds is None:
                raise ValueError(f"Empty sprite: {entry['biome']}")
            sprite = source.crop(bounds)
            sprite.thumbnail((16, 16), Image.Resampling.NEAREST)
            alpha = sprite.getchannel("A").point(lambda a: 255 if a >= 128 else 0)
            sprite = sprite.convert("RGB").quantize(
                colors=12, method=Image.Quantize.MAXCOVERAGE, dither=Image.Dither.NONE
            ).convert("RGBA")
            sprite.putalpha(alpha)
            for pixel_y in range(sprite.height):
                for pixel_x in range(sprite.width):
                    if not sprite.getpixel((pixel_x, pixel_y))[3]:
                        sprite.putpixel((pixel_x, pixel_y), (0, 0, 0, 0))
            canvas = Image.new("RGBA", (16, 16))
            canvas.paste(sprite, ((16 - sprite.width) // 2, 16 - sprite.height))
            output = DESTINATION / f"minecraft_{entry['biome']}.png"
            temporary = output.with_suffix(".png.tmp")
            canvas.save(temporary, format="PNG", optimize=True)
            temporary.replace(output)
        provenance.append({"biome": entry["biome"], "file": output.name, "prompt": entry["prompt"]})

    art_directory = ROOT / "art"
    art_directory.mkdir(exist_ok=True)
    (art_directory / "biome-sprite-prompts.json").write_text(
        json.dumps({"tool": "OpenAI built-in image_gen", "size": [16, 16], "sprites": provenance}, indent=2) + "\n",
        encoding="utf-8",
    )

    # Enlarged with nearest-neighbor so individual output pixels can be reviewed.
    columns, cell_width, cell_height = 6, 152, 110
    sheet = Image.new("RGB", (columns * cell_width, ((len(provenance) + columns - 1) // columns) * cell_height), "#24303a")
    draw = ImageDraw.Draw(sheet)
    for index, entry in enumerate(provenance):
        x, y = (index % columns) * cell_width, (index // columns) * cell_height
        with Image.open(DESTINATION / entry["file"]) as icon:
            enlarged = icon.resize((64, 64), Image.Resampling.NEAREST)
            sheet.paste(enlarged, (x + 44, y + 6), enlarged)
        draw.text((x + 4, y + 76), entry["biome"].replace("_", " "), fill="white")
    sheet.save(art_directory / "biome-sprites-preview.png")
    print(f"Exported {len(provenance)} native 16x16 biome sprites")


if __name__ == "__main__":
    export(Path(sys.argv[1]))
