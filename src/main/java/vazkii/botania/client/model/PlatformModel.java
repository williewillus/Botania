/**
 * This class was created by <williewillus>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.common.block.BlockCamo;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.TileCamo;

import java.util.List;

public class PlatformModel implements IBakedModel {

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		if(state.getBlock() != ModBlocks.platform)
			return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel().getQuads(state, side, rand);

		BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
		IBlockState heldState = ((IExtendedBlockState) state).getValue(BotaniaStateProps.HELD_STATE);
		IBlockAccess heldWorld = ((IExtendedBlockState) state).getValue(BotaniaStateProps.HELD_WORLD);
		BlockPos heldPos = ((IExtendedBlockState) state).getValue(BotaniaStateProps.HELD_POS);

		if (heldWorld == null || heldPos == null) {
			return ImmutableList.of();
		}

		Minecraft mc = Minecraft.getMinecraft();
		if(heldState == null && layer == BlockRenderLayer.SOLID) {
			// No camo
			ModelResourceLocation path = new ModelResourceLocation("botania:platform", "variant=" + state.getValue(BotaniaStateProps.PLATFORM_VARIANT).getName());
			return mc.getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(path).getQuads(state, side, rand);
		} else if(heldState != null) {
			if(heldState.getBlock().canRenderInLayer(heldState, layer)) {
				IBlockState actual = heldState.getBlock().getActualState(heldState, new FakeBlockAccess(heldWorld), heldPos);

				// Steal camo's model
				IBakedModel model = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelForState(actual);

				// Their model can be smart too
				IBlockState extended = heldState.getBlock().getExtendedState(actual, new FakeBlockAccess(heldWorld), heldPos);
				return model.getQuads(extended, side, rand);
			}
		}

		return ImmutableList.of(); // Nothing renders
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("botania:blocks/livingwood0");
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.NONE;
	}

	private static class FakeBlockAccess implements IBlockAccess {

		private final IBlockAccess compose;

		private FakeBlockAccess(IBlockAccess compose) {
			this.compose = compose;
		}

		@Override
		public TileEntity getTileEntity(BlockPos pos) {
			/* todo is this actually needed? if so this can't be an iblockaccess anymore
			IBlockState state = getBlockState(pos);
			if(state == null || !state.getBlock().hasTileEntity(state))
				return null;

			TileEntity tile = state.getBlock().createTileEntity(this, state);
			if(tile == null)
				return null;

			tile.setWorldObj(this);
			tile.setPos(pos);

			return tile;*/
			return compose.getTileEntity(pos);
		}

		@Override
		public int getCombinedLight(BlockPos pos, int lightValue) {
			return 15 << 20 | 15 << 4;
		}

		@Override
		public IBlockState getBlockState(BlockPos pos) {
			IBlockState state = compose.getBlockState(pos);
			if(state.getBlock() instanceof BlockCamo) {
				state = ((TileCamo) compose.getTileEntity(pos)).camoState;
			}
			return state == null ? Blocks.air.getDefaultState() : state;
		}

		@Override
		public boolean isAirBlock(BlockPos pos) {
			return compose.isAirBlock(pos);
		}

		@Override
		public BiomeGenBase getBiomeGenForCoords(BlockPos pos) {
			return compose.getBiomeGenForCoords(pos);
		}

		@Override
		public boolean extendedLevelsInChunkCache() {
			return compose.extendedLevelsInChunkCache();
		}

		@Override
		public int getStrongPower(BlockPos pos, EnumFacing direction) {
			return compose.getStrongPower(pos, direction);
		}

		@Override
		public WorldType getWorldType() {
			return compose.getWorldType();
		}

		@Override
		public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
			return compose.isSideSolid(pos, side, _default);
		}
	}

}
