package grondag.brocade.init;

import grondag.brocade.Brocade;
import grondag.brocade.block.SimpleBrocadeBlock;
import grondag.brocade.painting.PaintLayer;
import grondag.brocade.placement.BrocadeBlockItem;
import grondag.brocade.state.MeshState;
import grondag.brocade.state.MeshStateImpl;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BrocadeBlocks {
    
    
    public static final void init() {
        Brocade.LOG.debug("Registering Brocade Blocks");
        
        MeshState workingModel = new MeshStateImpl();
        workingModel.setShape(ModShapes.WEDGE);
        workingModel.setTexture(PaintLayer.BASE, BrocadeTextures.SANDSTONE_ZOOM2);
        workingModel.setColorRGB(PaintLayer.BASE, 0xFF656A70);
//        workingModel.setTexture(PaintLayer.MIDDLE, BrocadeTextures.BLOCK_COBBLE);
//        workingModel.setColorRGB(PaintLayer.MIDDLE, 0xFF4444);
//        workingModel.setTranslucent(PaintLayer.MIDDLE, true);
//        workingModel.setAlpha(PaintLayer.MIDDLE, 64);
//        workingModel.setEmissive(PaintLayer.MIDDLE, true);
//        workingModel.setTexture(PaintLayer.OUTER, BrocadeTextures.BORDER_CAUTION);
//        workingModel.setColorRGB(PaintLayer.OUTER, 0xD7FFFF);
//        workingModel.setColorRGB(PaintLayer.OUTER, 0xFFFFD300);
//        workingModel.setTranslucent(PaintLayer.OUTER, true);
//        workingModel.setEmissive(PaintLayer.OUTER, true);
        
        Block block = SimpleBrocadeBlock.create(
                FabricBlockSettings.of(Material.STONE).strength(1, 1).build(), 
                workingModel);
        
        register(block, "test");
    }
    
    private static void register(Block block, String name) {
        Identifier id = new Identifier("brocade", name);
        Registry.BLOCK.add(id, block);
        Registry.ITEM.add(id, new BrocadeBlockItem(block, 
                new Item.Settings().maxCount(64).group(ItemGroup.BUILDING_BLOCKS)));
    }
    
//  SuperModelBlock.registerSuperModelBlocks(event);
//
//  // TODO: disable test blocks
//  ISuperModelState workingModel;
//  workingModel = new ModelState();
//  workingModel.setShape(ModShapes.CSGTEST);
//  workingModel.setTexture(PaintLayer.BASE, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_STRONG);
//  workingModel.setColorRGB(PaintLayer.BASE, 0xFF9B898C);
//  workingModel.setTexture(PaintLayer.CUT, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_SUBTLE);
//  workingModel.setColorRGB(PaintLayer.CUT, 0xFF7F9BA6);
//
//  workingModel.setTexture(PaintLayer.LAMP, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_MODERATE);
//  workingModel.setColorRGB(PaintLayer.LAMP, 0xFFD5E9FF);
//  event.getRegistry().register(
//          new SuperSimpleBlock(Brocade.INSTANCE.prefixName("csgtest"), BlockSubstance.DEFAULT, workingModel)
//                  .setCreativeTab(Brocade.tabMod));
//
//
//  workingModel = new ModelState();
//  workingModel.setShape(ModShapes.SPHERE);
//  workingModel.setTexture(PaintLayer.BASE, grondag.brocade.init.BrocadeTextures.BLOCK_COBBLE);
//  workingModel.setColorRGB(PaintLayer.BASE, 0xBBC3C4);
//  event.getRegistry().register(new SuperSimpleBlock(Brocade.INSTANCE.prefixName("spheretest"),
//          BlockSubstance.DEFAULT, workingModel).setCreativeTab(Brocade.tabMod));
//
//  workingModel = new ModelState();
//  workingModel.setShape(ModShapes.COLUMN_SQUARE);
//  SquareColumnMeshFactory.setCutCount(3, workingModel);
//  SquareColumnMeshFactory.setCutsOnEdge(true, workingModel);
//  workingModel.setTexture(PaintLayer.BASE, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_MODERATE);
//  workingModel.setColorRGB(PaintLayer.BASE, BlockColorMapProvider.INSTANCE
//          .getColorMap(Hue.COBALT, Chroma.WHITE, Luminance.BRILLIANT).getColor(EnumColorMap.BASE));
//
//  workingModel.setTexture(PaintLayer.CUT, grondag.brocade.init.BrocadeTextures.BLOCK_NOISE_SUBTLE);
//  workingModel.setColorRGB(PaintLayer.CUT, BlockColorMapProvider.INSTANCE
//          .getColorMap(Hue.COBALT, Chroma.WHITE, Luminance.BRILLIANT).getColor(EnumColorMap.BASE));
//
//  workingModel.setTexture(PaintLayer.LAMP, grondag.brocade.init.BrocadeTextures.WHITE);
//  workingModel.setEmissive(PaintLayer.LAMP, true);
//  workingModel.setColorRGB(PaintLayer.LAMP, BlockColorMapProvider.INSTANCE
//          .getColorMap(Hue.CYAN, Chroma.RICH, Luminance.BRIGHT).getColor(EnumColorMap.LAMP));
//  event.getRegistry().register(
//          new SuperSimpleBlock(Brocade.INSTANCE.prefixName("coltest"), BlockSubstance.DEFAULT, workingModel)
//                  .setCreativeTab(Brocade.tabMod));
}
