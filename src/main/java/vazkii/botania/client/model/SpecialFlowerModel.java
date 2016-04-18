/**
 * This class was created by <williewillus>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.model;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelCustomData;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.commons.lang3.tuple.Pair;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.block.ItemBlockSpecialFlower;

import javax.vecmath.Matrix4f;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SpecialFlowerModel implements IModelCustomData {

	// SpecialFlowerModel for when there are no blockModels registered for a subtile
	public static final SpecialFlowerModel INSTANCE = new SpecialFlowerModel(ImmutableMap.of(), ImmutableMap.of());

	private final ImmutableMap<Optional<String>, ModelResourceLocation> blockModels;
	private final ImmutableMap<Optional<String>, ModelResourceLocation> itemModels;

	public SpecialFlowerModel(ImmutableMap<Optional<String>, ModelResourceLocation> blockModels,
							  ImmutableMap<Optional<String>, ModelResourceLocation> itemModels) {
		this.blockModels = blockModels;
		this.itemModels = itemModels;
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();
		builder.addAll(blockModels.values());
		builder.addAll(itemModels.values());

		// Force island models to be loaded and baked. See FloatingFlowerModel.
		builder.addAll(BotaniaAPIClient.getRegisteredIslandTypeModels().values());

		return builder.build();
	}

	@Override
	public Collection<ResourceLocation> getTextures() {
		return ImmutableList.of();
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		// "Bake" the SpecialFlowerModel (in reality, just create another wrapper for all the delegate blockModels)
		return new SpecialFlowerBakedModel(blockModels, itemModels, IPerspectiveAwareModel.MapWrapper.getTransforms(state));
	}

	@Override
	public IModelState getDefaultState() {
		return TRSRTransformation.identity();
	}

	@Override
	public IModel process(ImmutableMap<String, String> customData) {
		// Load the base variant from blockstate json, and also add all the model paths we received from external API
		ImmutableMap.Builder<Optional<String>, ModelResourceLocation> blockBuilder = ImmutableMap.builder();
		ImmutableMap.Builder<Optional<String>, ModelResourceLocation> itemBuilder = ImmutableMap.builder();

		for(String key : customData.keySet()) {
			if("base".equals(key)) {
				blockBuilder.put(Optional.absent(), getLocation(customData.get(key)));
			}
		}

		for(Map.Entry<String, ModelResourceLocation> e : BotaniaAPIClient.getRegisteredSubtileBlockModels().entrySet()) {
			blockBuilder.put(Optional.of(e.getKey()), e.getValue());
		}
		for(Map.Entry<String, ModelResourceLocation> e : BotaniaAPIClient.getRegisteredSubtileItemModels().entrySet()) {
			itemBuilder.put(Optional.of(e.getKey()), e.getValue());
		}

		ImmutableMap<Optional<String>, ModelResourceLocation> blockModels = blockBuilder.build();
		ImmutableMap<Optional<String>, ModelResourceLocation> itemModels = itemBuilder.build();
		if(blockModels.isEmpty() && itemModels.isEmpty()) return INSTANCE;
		return new SpecialFlowerModel(blockModels, itemModels);
	}

	private ModelResourceLocation getLocation(String json) {
		JsonElement e = new JsonParser().parse(json);
		if(e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
			return new ModelResourceLocation(e.getAsString());
		}
		FMLLog.severe("Expect ModelResourceLocation, got: ", json);
		return new ModelResourceLocation("builtin/missing", "missing");
	}

	public enum Loader implements ICustomModelLoader {
		INSTANCE {
			@Override
			public void onResourceManagerReload(IResourceManager resourceManager) {
			}

			@Override
			public boolean accepts(ResourceLocation modelLocation) {
				return modelLocation.getResourceDomain().equals("botania_special") && (
						modelLocation.getResourcePath().equals("specialFlower") ||
								modelLocation.getResourcePath().equals("models/block/specialFlower") ||
								modelLocation.getResourcePath().equals("models/item/specialFlower"));
			}

			@Override
			public IModel loadModel(ResourceLocation modelLocation) throws IOException {
				// Load a dummy model for now, all actual blockModels added in process().
				return SpecialFlowerModel.INSTANCE;
			}
		}
	}

	public static class SpecialFlowerBakedModel implements IPerspectiveAwareModel {

		private final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;
		private final ImmutableMap<Optional<String>, ModelResourceLocation> blockModels;
		private final ImmutableMap<Optional<String>, ModelResourceLocation> itemModels;

		private IBakedModel baseModel;
		private ImmutableMap<String, IBakedModel> bakedBlockModels;
		private ImmutableMap<String, IBakedModel> bakedItemModels;

		public SpecialFlowerBakedModel(ImmutableMap<Optional<String>, ModelResourceLocation> blockModels,
									   ImmutableMap<Optional<String>, ModelResourceLocation> itemModels, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> cameraTransforms) {
			this.blockModels = blockModels;
			this.itemModels = itemModels;
			this.transforms = cameraTransforms;
		}

		private void refreshBakedModels() {
			if(baseModel == null) {
				// If not done already, bake and cache all models
				baseModel = getBlockModel(Optional.absent());

				ImmutableMap.Builder<String, IBakedModel> builder = ImmutableMap.builder();
				for(Map.Entry<Optional<String>, ModelResourceLocation> e : blockModels.entrySet()) {
					if(e.getKey().isPresent()) {
						builder.put(e.getKey().get(), getBlockModel(e.getKey()));
					}
				}

				bakedBlockModels = builder.build();

				ImmutableMap.Builder<String, IBakedModel> builder2 = ImmutableMap.builder();
				for(Map.Entry<Optional<String>, ModelResourceLocation> e : itemModels.entrySet()) {
					if(e.getKey().isPresent()) {
						builder2.put(e.getKey().get(), getItemModel(e.getKey()));
					}
				}

				bakedItemModels = builder2.build();
			}
		}

		private IBakedModel getBlockModel(Optional<String> opt) {
			ModelResourceLocation loc = blockModels.get(opt);
			if(loc == null) {
				loc = new ModelResourceLocation("builtin/missing", "missing");
			}
			IModel model = ModelLoaderRegistry.getModelOrMissing(loc);
			return model.bake(new SimpleModelState(transforms), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
		}

		private IBakedModel getItemModel(Optional<String> opt) {
			ModelResourceLocation loc = itemModels.get(opt);
			if(loc == null) {
				loc = new ModelResourceLocation("builtin/missing", "missing");
			}
			IModel model = ModelLoaderRegistry.getModelOrMissing(loc);
			return model.bake(new SimpleModelState(transforms), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
		}

		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing face, long rand) {
			if(state.getBlock() != ModBlocks.specialFlower)
				return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel().getQuads(state, face, rand);
			refreshBakedModels();
			IExtendedBlockState extendedState = ((IExtendedBlockState) state);
			IBakedModel ret = bakedBlockModels.get(extendedState.getValue(BotaniaStateProps.SUBTILE_ID));
			if(ret == null)
				ret = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
			return ret.getQuads(state, face, rand);
		}

		@Override
		public ItemOverrideList getOverrides() {
			return itemHandler;
		}

		private final ItemOverrideList itemHandler = new ItemOverrideList(ImmutableList.of()) {
			@Override
			public IBakedModel handleItemState(IBakedModel original, ItemStack stack, World world, EntityLivingBase living) {
				refreshBakedModels();
				IBakedModel item = bakedItemModels.get(ItemBlockSpecialFlower.getType(stack));
				if(item == null) {
					item = bakedBlockModels.get(ItemBlockSpecialFlower.getType(stack));
				}

				return item == null ? Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel() : item;
			}
		};

		@Override
		public boolean isAmbientOcclusion() {
			refreshBakedModels();
			return baseModel.isAmbientOcclusion();
		}

		@Override
		public boolean isGui3d() {
			refreshBakedModels();
			return baseModel.isGui3d();
		}

		@Override
		public boolean isBuiltInRenderer() {
			refreshBakedModels();
			return baseModel.isBuiltInRenderer();
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			refreshBakedModels();
			return baseModel.getParticleTexture();
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return baseModel.getItemCameraTransforms();
		}

		@Override
		public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
			return IPerspectiveAwareModel.MapWrapper.handlePerspective(this, transforms, cameraTransformType);
		}
	}

}
