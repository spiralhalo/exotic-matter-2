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

package grondag.xm.paint;

import java.io.Reader;
import java.util.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import grondag.xm.api.paint.XmPaintFinder;
import grondag.xm.api.texture.TextureSetRegistry;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

class PaintDeserializer {
	public static XmPaintImpl.Value deserialize(Reader reader) {
		final XmPaintImpl.Finder finder = XmPaintImpl.finder();
		final JsonObject json = JsonHelper.deserialize(reader);

		if (json.has("layers")) {
			final JsonArray layers = JsonHelper.asArray(json.get("layers"), "layers");
			if(!layers.isJsonNull()) {
				final int depth = layers.size();
				if(depth > 3) return null;
				finder.textureDepth(depth);
				for(int i = 0; i < depth; i++) {
					readLayer(layers.get(i).getAsJsonObject(), finder, i);
				}
			}
		}
		return finder.find();
	}

	private static void readLayer(JsonObject layer, XmPaintFinder finder, int spriteIndex) {
		if (layer.has("disableAo")) {
			finder.disableAo(spriteIndex, JsonHelper.getBoolean(layer, "disableAo", true));
		}

		if (layer.has("disableColorIndex")) {
			finder.disableColorIndex(spriteIndex, JsonHelper.getBoolean(layer, "disableColorIndex", true));
		}

		if (layer.has("disableDiffuse")) {
			finder.disableDiffuse(spriteIndex, JsonHelper.getBoolean(layer, "disableDiffuse", true));
		}

		if (layer.has("emissive")) {
			finder.emissive(spriteIndex, JsonHelper.getBoolean(layer, "emissive", true));
		}

		if (layer.has("blendMode")) {
			finder.blendMode(spriteIndex, readBlendMode(JsonHelper.getString(layer, "blendMode")));
		}

		if (layer.has("color")) {
			finder.textureColor(spriteIndex, color(JsonHelper.getString(layer, "color")));
		}

		if (layer.has("texture")) {
			finder.texture(spriteIndex, TextureSetRegistry.instance().get(new Identifier(JsonHelper.getString(layer, "texture"))));
		}
	}

	private static BlendMode readBlendMode(String val) {
		val = val.toLowerCase(Locale.ROOT);
		switch(val) {
		case "solid":
		default:
			return BlendMode.SOLID;
		case "cutout":
			return BlendMode.CUTOUT;
		case "cutout_mipped":
			return BlendMode.CUTOUT_MIPPED;
		case "translucent":
			return BlendMode.TRANSLUCENT;
		}
	}

	private static int color(String str) {
		return str.startsWith("0x") ? Integer.parseUnsignedInt(str.substring(2), 16) : Integer.parseInt(str);
	}
}
