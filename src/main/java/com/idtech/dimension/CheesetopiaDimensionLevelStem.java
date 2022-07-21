package com.idtech.dimension;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static net.minecraft.world.level.dimension.LevelStem.*;

public class CheesetopiaDimensionLevelStem extends ResourceKey<LevelStem> {
    public static final Codec<LevelStem> CODEC = RecordCodecBuilder.create((p_63986_) -> {
        return p_63986_.group(DimensionType.CODEC.fieldOf("type").flatXmap(ExtraCodecs.nonNullSupplierCheck(),
                ExtraCodecs.nonNullSupplierCheck()).forGetter(LevelStem::typeSupplier),
                ChunkGenerator.CODEC.fieldOf("generator").forGetter(LevelStem::generator)).apply(p_63986_,
                p_63986_.stable(LevelStem::new));
    });

    //new
    //the constructor has been a massive roadblock, possibly specifying a builder through each quality would achieve it?
    public CheesetopiaDimensionLevelStem(Supplier<DimensionType> dimenType, ChunkGenerator chunkGen) {
        super(dimenType, chunkGen);
        //super has private access, so possibly rebuilding that here in the constructor would work.

        this.type = dimenType;
        this.generator = chunkGen;
    }
    public static final ResourceKey<LevelStem> CHEESETOPIA = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("cheestopia"));
    //new ^^^^
    //This is where we set up the link between this LevelStem and our DimensionType's CHEESETOPIA_EFFECTS and other properties.
    private static final Set<ResourceKey<LevelStem>> BUILTIN_ORDER = Sets.newLinkedHashSet(ImmutableList.of(OVERWORLD, NETHER, END, CHEESETOPIA));
    //new ^^^^
    //this is where we add CHEESETOPIA, which carries the Registry.LEVEL_STEM_REGISTRY and ResourceLocation("cheestopia")
    // into the BUILTIN_ORDER Set of Immutable Lists for registration and some other checks in the sortMap and stable methods.
    private final Supplier<DimensionType> type;
    private final ChunkGenerator generator;

    public Supplier<DimensionType> typeSupplier() {
        return this.type;
    }

    public DimensionType type() {
        return this.type.get();
    }

    public ChunkGenerator generator() {
        return this.generator;
    }

    public static MappedRegistry<LevelStem> sortMap(MappedRegistry<LevelStem> p_63988_) {
        MappedRegistry<LevelStem> mappedregistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());

        for(ResourceKey<LevelStem> resourcekey : BUILTIN_ORDER) {
            LevelStem levelstem = p_63988_.get(resourcekey);
            if (levelstem != null) {
                mappedregistry.register(resourcekey, levelstem, p_63988_.lifecycle(levelstem));
            }
        }

        for(Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : p_63988_.entrySet()) {
            ResourceKey<LevelStem> resourcekey1 = entry.getKey();
            if (!BUILTIN_ORDER.contains(resourcekey1)) {
                mappedregistry.register(resourcekey1, entry.getValue(), p_63988_.lifecycle(entry.getValue()));
            }
        }

        return mappedregistry;
    }




    public static boolean stable(long p_63983_, MappedRegistry<LevelStem> p_63984_) {
        List<Map.Entry<ResourceKey<LevelStem>, LevelStem>> list = Lists.newArrayList(p_63984_.entrySet());
        if (list.size() != BUILTIN_ORDER.size()) {
            return false;
        } else {
            Map.Entry<ResourceKey<LevelStem>, LevelStem> entry = list.get(0);
            Map.Entry<ResourceKey<LevelStem>, LevelStem> entry1 = list.get(1);
            Map.Entry<ResourceKey<LevelStem>, LevelStem> entry2 = list.get(2);
            //new
            //entry 3 is the position in the BUILTIN_ORDER set of imm list that cheesetopia will be in.
            //I am not currently certain if we need to do anything with it in here.
            Map.Entry<ResourceKey<LevelStem>, LevelStem> entry3 = list.get(3);
            if (entry.getKey() == OVERWORLD && entry1.getKey() == NETHER && entry2.getKey() == END) {
                if (!entry.getValue().type().equalTo(CheesetopiaDimensionType.DEFAULT_OVERWORLD) && entry.getValue().type() != CheesetopiaDimensionType.DEFAULT_OVERWORLD_CAVES) {
                    return false;
                } else if (!entry1.getValue().type().equalTo(CheesetopiaDimensionType.DEFAULT_NETHER)) {
                    return false;
                } else if (!entry2.getValue().type().equalTo(CheesetopiaDimensionType.DEFAULT_END)) {
                    return false;
                } else if (entry1.getValue().generator() instanceof NoiseBasedChunkGenerator && entry2.getValue().generator() instanceof NoiseBasedChunkGenerator) {
                    NoiseBasedChunkGenerator noisebasedchunkgenerator = (NoiseBasedChunkGenerator)entry1.getValue().generator();
                    NoiseBasedChunkGenerator noisebasedchunkgenerator1 = (NoiseBasedChunkGenerator)entry2.getValue().generator();
                    if (!noisebasedchunkgenerator.stable(p_63983_, NoiseGeneratorSettings.NETHER)) {
                        return false;
                    } else if (!noisebasedchunkgenerator1.stable(p_63983_, NoiseGeneratorSettings.END)) {
                        return false;
                    } else if (!(noisebasedchunkgenerator.getBiomeSource() instanceof MultiNoiseBiomeSource)) {
                        return false;
                    } else {
                        MultiNoiseBiomeSource multinoisebiomesource = (MultiNoiseBiomeSource)noisebasedchunkgenerator.getBiomeSource();
                        if (!multinoisebiomesource.stable(MultiNoiseBiomeSource.Preset.NETHER)) {
                            return false;
                        } else {
                            BiomeSource biomesource = entry.getValue().generator().getBiomeSource();
                            if (biomesource instanceof MultiNoiseBiomeSource && !((MultiNoiseBiomeSource)biomesource).stable(MultiNoiseBiomeSource.Preset.OVERWORLD)) {
                                return false;
                            } else if (!(noisebasedchunkgenerator1.getBiomeSource() instanceof TheEndBiomeSource)) {
                                return false;
                            } else {
                                TheEndBiomeSource theendbiomesource = (TheEndBiomeSource)noisebasedchunkgenerator1.getBiomeSource();
                                return theendbiomesource.stable(p_63983_);
                            }
                        }
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }
}
