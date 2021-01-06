package hunternif.mc.impl.atlas.core;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;

import javax.annotation.Nullable;

/**
 * Finds the biome ID to be used for a given chunk.
 * @author Hunternif
 */
interface IBiomeDetector {
	/** Finds the biome ID to be used for a given chunk. */
	@Nullable
	ResourceLocation getBiomeID(World world, IChunk chunk);
}
