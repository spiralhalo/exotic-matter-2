package grondag.xm2.texture;

import static grondag.xm2.api.texture.TextureGroup.STATIC_BORDERS;
import static grondag.xm2.api.texture.TextureLayoutMap.BORDER_13;
import static grondag.xm2.api.texture.TextureLayoutMap.BORDER_14;
import static grondag.xm2.api.texture.TextureRenderIntent.OVERLAY_ONLY;
import static grondag.xm2.api.texture.TextureRotation.ROTATE_NONE;
import static grondag.xm2.api.texture.TextureRotation.ROTATE_RANDOM;
import static grondag.xm2.api.texture.TextureScale.SINGLE;

import grondag.xm2.api.texture.TextureGroup;
import grondag.xm2.api.texture.TextureLayoutMap;
import grondag.xm2.api.texture.TextureRenderIntent;
import grondag.xm2.api.texture.TextureRotation;
import grondag.xm2.api.texture.TextureScale;
import grondag.xm2.api.texture.TextureSet;

public abstract class TextureSetHelper {
    private TextureSetHelper() {}

    static final TextureGroup[] BORDERS_STATIC = new TextureGroup[] { TextureGroup.STATIC_BORDERS };
    static final TextureGroup[] DUAL_STATIC =  new TextureGroup[] { TextureGroup.STATIC_TILES, TextureGroup.STATIC_BORDERS };
    
    public static TextureSet addBorderSingle(String modId, String name) {
        return TextureSet.builder().displayNameToken(name).baseTextureName(modId + ":blocks/" + name).versionCount(1).scale(SINGLE).layout(BORDER_13)
                .rotation(ROTATE_NONE).renderIntent(OVERLAY_ONLY).groups(STATIC_BORDERS).build(modId + ":" + name);
    }
    
    public static TextureSet addBorderRandom(String modId, String name, boolean allowTile, boolean renderNoBorderAsTile) {
        return TextureSet.builder().displayNameToken(name).baseTextureName(modId + ":blocks/" + name).versionCount(4).scale(SINGLE)
                .layout(renderNoBorderAsTile ? BORDER_14 : BORDER_13)
                .renderIntent(allowTile ? TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT : TextureRenderIntent.OVERLAY_ONLY)
                .groups(allowTile ? BORDERS_STATIC : DUAL_STATIC).rotation(ROTATE_NONE).build(modId + ":" + name);
    }

    public static TextureSet addBigTex(String modId, String name) {
        return TextureSet.builder().displayNameToken(name).baseTextureName(modId + ":blocks/" + name).versionCount(1).scale(TextureScale.MEDIUM)
                .layout(TextureLayoutMap.SINGLE).rotation(ROTATE_RANDOM).renderIntent(TextureRenderIntent.BASE_ONLY).groups(TextureGroup.STATIC_TILES)
                .build(modId + ":" + name);
    }

    public static TextureSet addZoom(TextureSet template) {
        return TextureSet.builder(template).scale(TextureScale.LARGE).displayNameToken(template.displayNameToken() + "_zoom")
                .build(template.id().toString() + "_zoom");
    }

    public static TextureSet addZoom2(TextureSet template) {
        return TextureSet.builder(template).scale(TextureScale.GIANT).displayNameToken(template.displayNameToken() + "_zoom2")
                .build(template.id().toString() + "_zoom2");
    }

    public static TextureSet addDecal(String modId, String idName, String fileName, TextureRotation rotation) {
        return TextureSet.builder().displayNameToken(idName).baseTextureName(modId + ":blocks/" + fileName).versionCount(1).scale(TextureScale.SINGLE)
                .layout(TextureLayoutMap.SINGLE).rotation(rotation).renderIntent(TextureRenderIntent.OVERLAY_ONLY).groups(TextureGroup.STATIC_DETAILS)
                .build(modId + ":" + idName);
    }
}
