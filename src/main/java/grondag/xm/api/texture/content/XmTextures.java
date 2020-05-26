/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.xm.api.texture.content;

import static grondag.xm.api.texture.TextureGroup.ALWAYS_HIDDEN;
import static grondag.xm.api.texture.TextureGroup.STATIC_BORDERS;
import static grondag.xm.api.texture.TextureGroup.STATIC_TILES;
import static grondag.xm.api.texture.TextureLayoutMap.QUADRANT_ROTATED_SINGLE;
import static grondag.xm.api.texture.TextureRenderIntent.BASE_ONLY;
import static grondag.xm.api.texture.TextureRenderIntent.BASE_OR_OVERLAY_CUTOUT_OKAY;
import static grondag.xm.api.texture.TextureRenderIntent.OVERLAY_ONLY;
import static grondag.xm.api.texture.TextureScale.SINGLE;
import static grondag.xm.api.texture.TextureTransform.IDENTITY;
import static grondag.xm.api.texture.TextureTransform.ROTATE_RANDOM;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

import grondag.xm.api.texture.TextureGroup;
import grondag.xm.api.texture.TextureLayout;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;

@API(status = EXPERIMENTAL)
public enum XmTextures {
	;

	public static final TextureSet TILE_NOISE_STRONG = TextureSet.builder()
			.displayNameToken("noise_strong").baseTextureName("exotic-matter:block/noise_strong")
			.versionCount(4).scale(SINGLE).layout(TextureLayoutMap.VERSION_X_8).transform(ROTATE_RANDOM)
			.renderIntent(BASE_ONLY).groups(STATIC_TILES).build("exotic-matter:noise_strong");

	public static final TextureSet TILE_NOISE_MODERATE = TextureSet.builder(TILE_NOISE_STRONG).displayNameToken("noise_moderate")
			.baseTextureName("exotic-matter:block/noise_moderate").build("exotic-matter:noise_moderate");

	public static final TextureSet TILE_NOISE_LIGHT = TextureSet.builder(TILE_NOISE_STRONG).displayNameToken("noise_light")
			.baseTextureName("exotic-matter:block/noise_light").build("exotic-matter:noise_light");

	public static final TextureSet TILE_NOISE_SUBTLE = TextureSet.builder(TILE_NOISE_STRONG).displayNameToken("noise_subtle")
			.baseTextureName("exotic-matter:block/noise_subtle").build("exotic-matter:noise_subtle");

	public static final TextureSet TILE_NOISE_EXTREME = TextureSet.builder()
			.displayNameToken("noise_extreme").baseTextureName("exotic-matter:block/noise_extreme")
			.versionCount(4).scale(SINGLE).layout(TextureLayoutMap.VERSION_X_8).transform(ROTATE_RANDOM)
			.renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_TILES).build("exotic-matter:noise_extreme");

	public static final TextureSet WHITE = TextureSet.builder().displayNameToken("white").baseTextureName("exotic-matter:block/white").versionCount(1).scale(SINGLE)
			.layout(TextureLayoutMap.VERSION_X_8).transform(IDENTITY).groups(STATIC_TILES).build("exotic-matter:white");

	/** Used as filler in mixed quadrants */
	public static final TextureSet EMPTY = TextureSet.builder().displayNameToken("empty").baseTextureName("exotic-matter:block/empty").versionCount(1).scale(SINGLE)
			.layout(TextureLayoutMap.SINGLE).transform(IDENTITY).groups(ALWAYS_HIDDEN).build("exotic-matter:empty");

	public static final TextureSet BORDER_SINGLE_LINE = TextureSet.builder().displayNameToken("border_single_line")
			.baseTextureName("exotic-matter:block/border_single_line").versionCount(1).scale(SINGLE).layout(QUADRANT_ROTATED_SINGLE).transform(IDENTITY)
			.renderIntent(OVERLAY_ONLY).groups(STATIC_BORDERS).build("exotic-matter:border_single_line");


	public static final TextureSet TILE_NOISE_BLUE_A = TextureSet.builder().displayNameToken("blue_noise_a")
			.baseTextureName("exotic-matter:block/noise_blue_0").versionCount(4)
			.scale(TextureScale.SINGLE).layout(TextureLayoutMap.VERSIONED)
			.transform(ROTATE_RANDOM).renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY)
			.groups(TextureGroup.STATIC_TILES, TextureGroup.STATIC_DETAILS).build("exotic-matter:blue_noise_a");

	public static final TextureSet TILE_NOISE_BLUE_B = TextureSet.builder().displayNameToken("blue_noise_b")
			.baseTextureName("exotic-matter:block/noise_blue_1").versionCount(4)
			.scale(TextureScale.SINGLE).layout(TextureLayoutMap.VERSIONED)
			.transform(ROTATE_RANDOM).renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY)
			.groups(TextureGroup.STATIC_TILES, TextureGroup.STATIC_DETAILS).build("exotic-matter:blue_noise_b");

	public static final TextureLayoutMap NOISE_LAYOUT = TextureLayoutMap.create(TextureLayout.SIMPLE, (s, v, i) -> s + (v < 4 ? "_0_" + v : "_1_" + (v - 4)));

	public static final TextureSet TILE_NOISE_BLUE = TextureSet.builder().displayNameToken("blue_noise")
			.baseTextureName("exotic-matter:block/noise_blue").versionCount(8)
			.scale(TextureScale.SINGLE).layout(NOISE_LAYOUT)
			.transform(ROTATE_RANDOM).renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY)
			.groups(TextureGroup.STATIC_TILES, TextureGroup.STATIC_DETAILS).build("exotic-matter:blue_noise_b");


}
