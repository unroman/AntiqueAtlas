package hunternif.mc.impl.atlas.structure;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import com.google.common.collect.HashMultimap;

import hunternif.mc.impl.atlas.AntiqueAtlasMod;
import hunternif.mc.impl.atlas.api.AtlasAPI;
import hunternif.mc.impl.atlas.registry.MarkerType;
import hunternif.mc.impl.atlas.util.MathUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.server.ServerWorld;

public class StructureHandler {
	private static final HashMultimap<ResourceLocation, Tuple<ResourceLocation, Setter>> STRUCTURE_PIECE_TO_TILE_MAP = HashMultimap.create();
	private static final HashMap<ResourceLocation, Tuple<ResourceLocation, ITextComponent>> STRUCTURE_PIECE_TO_MARKER_MAP = new HashMap<>();
	private static final HashMap<ResourceLocation, Integer> STRUCTURE_PIECE_TILE_PRIORITY = new HashMap<>();
	private static final Setter ALWAYS = (box) -> Collections.singleton(new ChunkPos(MathUtil.getCenter(box).getX() >> 4, MathUtil.getCenter(box).getZ() >> 4));

	public static void registerTile(IStructurePieceType structurePieceType, int priority, ResourceLocation textureId, Setter setter) {
		ResourceLocation id = Registry.STRUCTURE_PIECE.getKey(structurePieceType);
		STRUCTURE_PIECE_TO_TILE_MAP.put(id, new Tuple<>(textureId, setter));
		STRUCTURE_PIECE_TILE_PRIORITY.put(id, priority);
	}

	public static void registerTile(IStructurePieceType structurePieceType, int priority, ResourceLocation textureId) {
		registerTile(structurePieceType, priority, textureId, ALWAYS);
	}

	public static void registerMarker(Structure<?> structureFeature, ResourceLocation markerType, ITextComponent name) {
		STRUCTURE_PIECE_TO_MARKER_MAP.put(Registry.STRUCTURE_FEATURE.getKey(structureFeature), new Tuple<>(markerType, name));
	}

	private static int getPriority(ResourceLocation structurePieceId) {
		return STRUCTURE_PIECE_TILE_PRIORITY.getOrDefault(structurePieceId, Integer.MAX_VALUE);
	}

	private static void put(ResourceLocation structurePieceId, World world, int chunkX, int chunkZ, ResourceLocation textureId) {
		ResourceLocation existingTile = AntiqueAtlasMod.tileData.getData(world.getDimensionKey()).getTile(chunkX, chunkZ);

		if (getPriority(structurePieceId) < getPriority(existingTile)) {
			AtlasAPI.tiles.putCustomGlobalTile(world, textureId, chunkX, chunkZ);
		}
	}

	public static void resolve(StructurePiece structurePiece, ServerWorld world) {
		ResourceLocation structurePieceId = Registry.STRUCTURE_PIECE.getKey(structurePiece.getStructurePieceType());
		if (STRUCTURE_PIECE_TO_TILE_MAP.containsKey(structurePieceId)) {
			for (Tuple<ResourceLocation, Setter> entry : STRUCTURE_PIECE_TO_TILE_MAP.get(structurePieceId)) {
				Collection<ChunkPos> matches = entry.getB().matches(structurePiece.getBoundingBox());

				for (ChunkPos pos : matches) {
					put(structurePieceId, world, pos.x, pos.z, entry.getA());
				}
			}
		}
	}

	public static void resolve(StructureStart<?> structureStart, ServerWorld world) {
		ResourceLocation structureId = Registry.STRUCTURE_FEATURE.getKey(structureStart.getStructure());
		if (STRUCTURE_PIECE_TO_MARKER_MAP.containsKey(structureId)) {
			AtlasAPI.markers.putGlobalMarker(
					world,
					false,
					MarkerType.REGISTRY.getOrDefault(STRUCTURE_PIECE_TO_MARKER_MAP.get(structureId).getA()),
					STRUCTURE_PIECE_TO_MARKER_MAP.get(structureId).getB(),
					structureStart.getBoundingBox()./*getCenter*/func_215126_f().getX(),
					structureStart.getBoundingBox()./*getCenter*/func_215126_f().getZ()
			);
		}
	}

	interface Setter {
		Collection<ChunkPos> matches(MutableBoundingBox box);
	}
}
