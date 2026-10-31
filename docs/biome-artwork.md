# Biome sprites

The biome icons are original artwork generated for this project with the built-in
OpenAI image generation tool, then exported as **16 by 16 pixel RGBA PNGs**. The
export uses nearest-neighbor sampling, a maximum of 12 opaque colors, and binary
transparency. They are static list icons and cannot be enlarged, rotated, or
zoomed in the tracker.

No Minecraft Wiki image files or extracted Minecraft texture files are used in
this replacement set. The written generation prompts are recorded in
[`art/biome-sprite-prompts.json`](../art/biome-sprite-prompts.json). An enlarged
nearest-neighbor preview is in `art/biome-sprites-preview.png`.

`tools/export_biome_sprites.py` reproduces the export from local generation
manifests. Those manifests point to the original generated PNGs on the author's
machine; they are build intermediates rather than runtime dependencies.

## Why these do not use Wiki sprites

Licence review: 31 October 2026.

The Minecraft Wiki's general site licence is CC BY-NC-SA 3.0, with exceptions.
The individual file notices for
[plains](https://minecraft.wiki/w/File:BiomeSprite_plains.png) and
[bamboo jungle](https://minecraft.wiki/w/File:BiomeSprite_bamboo-jungle.png)
instead identify Mojang copyright and describe permission for use on the Wiki.
The general Creative Commons footer is therefore not sufficient evidence to
relicense those files as part of this mod. This review did not establish that
all Wiki sprites are prohibited in mods; it did not establish an explicit grant
for redistributing those particular redraws either.

Mojang permits mod distribution subject to its
[EULA](https://www.minecraft.net/en-us/eula) and
[Usage Guidelines](https://www.minecraft.net/en-us/usage-guidelines).
The original replacement set avoids relying on an interpretation of the Wiki
images' file-specific permissions. Minecraft names and other existing game
content remain subject to their respective owners' rights.

## Project distribution

This version uses the project's all-rights-reserved notice in `LICENSE`,
with matching Fabric metadata. It grants permission to download,
install, and play official releases. Redistribution, including inclusion in
modpacks, requires prior written permission. The notice is included in release
and source JARs. Permissions previously granted for older releases are not
revoked by this change.

An all-rights-reserved notice only reserves rights that actually exist. Exclusive
copyright in AI-generated artwork is not guaranteed: the
[European Commission IP Helpdesk](https://intellectual-property-helpdesk.ec.europa.eu/regional-ip-helpdesks/european-ip-helpdesk/europe-frequently-asked-questions_en)
describes sufficient human intellectual contribution as a requirement under EU
copyright law. This project does not represent the notice as a guarantee of
exclusive copyright in automatically generated pixels.
