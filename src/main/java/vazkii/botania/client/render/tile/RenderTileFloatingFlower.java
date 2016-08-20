/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jul 8, 2014, 10:58:46 PM (GMT)]
 */
package vazkii.botania.client.render.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.apache.commons.lang3.tuple.Pair;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.core.handler.ConfigHandler;

import javax.annotation.Nonnull;
import javax.vecmath.Vector3f;
import java.util.Random;

public class RenderTileFloatingFlower extends FastTESR {

	@Override
	public void renderTileEntityFast(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {
		if(ConfigHandler.staticFloaters)
			return;

		BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos pos = te.getPos();
		IBlockAccess world = MinecraftForgeClient.getRegionRenderCache(te.getWorld(), pos);
		IBlockState state = world.getBlockState(pos);

		IExtendedBlockState exState = (IExtendedBlockState) state.getBlock().getExtendedState(state, world, pos);

		float time = ClientTickHandler.ticksInGame + partialTicks;
		if(te.getWorld() != null)
			time += new Random(te.getPos().hashCode()).nextInt(1000);

		// TODO: caching?
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelManager().getModel(new ModelResourceLocation("botania:floatingSpecialFlower", "inventory"));
		TRSRTransformation rotate1 = new TRSRTransformation(null, TRSRTransformation.quatFromXYZDegrees(new Vector3f(0, -(time * 0.5F), 0)), null, null);
		TRSRTransformation translate1 = new TRSRTransformation(new Vector3f(0, (float) Math.sin(time * 0.05F) * 0.1F, 0), null, null, null);
		TRSRTransformation rotate2 = new TRSRTransformation(null, TRSRTransformation.quatFromXYZDegrees(new Vector3f(4F * (float) Math.sin(time * 0.04F), 0, 0)), null, null);
		TRSRTransformation finalTransform = rotate1.compose(translate1).compose(rotate2);
		exState = exState.withProperty(Properties.AnimationProperty, finalTransform);

		vb.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());

		blockRenderer.getBlockModelRenderer().renderModel(world, model, exState, pos, vb, false);
	}
}
