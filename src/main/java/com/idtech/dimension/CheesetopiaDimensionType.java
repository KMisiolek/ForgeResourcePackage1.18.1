package com.idtech.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.DimensionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import static net.minecraft.world.level.dimension.DimensionType.*;

/*
There's a whole bunch of stuff in the DimensionType class file that is private or inaccessible, so we're going to make
our own DimensionType that includes our dimension ("cheestopia", for some example) and also accomodates the other
dimensions that we can use to register everything in the registry on game load.
Any line with "//new" or "//new dimension" is added in to accomodate our dimension, or altered from
    net.minecraft.world.level.dimension.DimensionType; . Cheers, happy digging!
 */
public class CheesetopiaDimensionType{

    public static final int BITS_FOR_Y = BlockPos.PACKED_Y_LENGTH;
    public static final int MIN_HEIGHT = 16;
    public static final int Y_SIZE = (1 << BITS_FOR_Y) - 32;
    public static final int MAX_Y = (Y_SIZE >> 1) - 1;
    public static final int MIN_Y = MAX_Y - Y_SIZE + 1;
    public static final int WAY_ABOVE_MAX_Y = MAX_Y << 4;
    public static final int WAY_BELOW_MIN_Y = MIN_Y << 4;

    //These ResourceLocations are not private in DimensionType so we can just copy them over.
    // What values they are assigned are commented below.
    public static final ResourceLocation OVERWORLD_EFFECTS = DimensionType.OVERWORLD_EFFECTS;
    public static final ResourceLocation NETHER_EFFECTS = DimensionType.NETHER_EFFECTS;
    public static final ResourceLocation END_EFFECTS = DimensionType.END_EFFECTS;
    /* as seen in DimensionType:
       public static final ResourceLocation OVERWORLD_EFFECTS = new ResourceLocation("overworld");
   public static final ResourceLocation NETHER_EFFECTS = new ResourceLocation("the_nether");
   public static final ResourceLocation END_EFFECTS = new ResourceLocation("the_end");
     */

    //new
    //This is as I'm aware this is the name we'll use in en_us.json and other similar places.
    public static final ResourceLocation CHEESETOPIA_EFFECTS = new ResourceLocation("cheesetopia");

    //new
    //We change the type the Codec parameterizes, we might be able to leave this as generic instead...
    public static final Codec<CheesetopiaDimensionType> DIRECT_CODEC = RecordCodecBuilder.<CheesetopiaDimensionType>create((p_63914_) -> {
        return p_63914_.group(Codec.LONG.optionalFieldOf("fixed_time").xmap((p_156696_) -> {
            return p_156696_.map(OptionalLong::of).orElseGet(OptionalLong::empty);
        },
        (p_156698_) -> {
            return p_156698_.isPresent() ? Optional.of(p_156698_.getAsLong()) : Optional.empty();
        }).forGetter((p_156731_) -> {
            return p_156731_.fixedTime;
        }),
        Codec.BOOL.fieldOf("has_skylight").forGetter(CheesetopiaDimensionType::hasSkyLight),
        Codec.BOOL.fieldOf("has_ceiling").forGetter(CheesetopiaDimensionType::hasCeiling),
        Codec.BOOL.fieldOf("ultrawarm").forGetter(CheesetopiaDimensionType::ultraWarm),
        Codec.BOOL.fieldOf("natural").forGetter(CheesetopiaDimensionType::natural),
        Codec.doubleRange((double)1.0E-5F, 3.0E7D).fieldOf("coordinate_scale").forGetter(CheesetopiaDimensionType::coordinateScale),
        Codec.BOOL.fieldOf("piglin_safe").forGetter(CheesetopiaDimensionType::piglinSafe),
        Codec.BOOL.fieldOf("bed_works").forGetter(CheesetopiaDimensionType::bedWorks),
        Codec.BOOL.fieldOf("respawn_anchor_works").forGetter(CheesetopiaDimensionType::respawnAnchorWorks),
        Codec.BOOL.fieldOf("has_raids").forGetter(CheesetopiaDimensionType::hasRaids),
        Codec.intRange(MIN_Y, MAX_Y).fieldOf("min_y").forGetter(CheesetopiaDimensionType::minY),
        Codec.intRange(16, Y_SIZE).fieldOf("height").forGetter(CheesetopiaDimensionType::height),
        Codec.intRange(0, Y_SIZE).fieldOf("logical_height").forGetter(CheesetopiaDimensionType::logicalHeight),
        ResourceLocation.CODEC.fieldOf("infiniburn").forGetter((p_156729_) -> {
            return p_156729_.infiniburn;
        }),
        ResourceLocation.CODEC.fieldOf("effects").orElse(OVERWORLD_EFFECTS).forGetter((p_156725_) -> {
            return p_156725_.effectsLocation;
        }),
        Codec.FLOAT.fieldOf("ambient_light").forGetter((p_156721_) -> {
            return p_156721_.ambientLight;
        })).apply(p_63914_, CheesetopiaDimensionType::new);
        //new ^^^^
        // the .apply(p_63914_, DimensionType::new); field above cannot use DimensionType::new as it has private access
        //in DimensionType, so we change it to Required type:
        //Function16
        //<java.util.OptionalLong,
        //java.lang.Boolean,
        //java.lang.Boolean,
        //java.lang.Boolean,
        //java.lang.Boolean,
        //java.lang.Double,
        //java.lang.Boolean,
        //java.lang.Boolean,
        //java.lang.Boolean,
        //java.lang.Boolean,
        //java.lang.Integer,
        //java.lang.Integer,
        //java.lang.Integer,
        //net.minecraft.resources.ResourceLocation,
        //net.minecraft.resources.ResourceLocation,
        //java.lang.Float,
        //R>
    }).comapFlatMap(CheesetopiaDimensionType::guardY, Function.identity());


    private static final int MOON_PHASES = 8;
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
    public static final ResourceKey<DimensionType> OVERWORLD_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY,
            new ResourceLocation("overworld"));
    public static final ResourceKey<DimensionType> NETHER_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY,
            new ResourceLocation("the_nether"));
    public static final ResourceKey<DimensionType> END_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY,
            new ResourceLocation("the_end"));

    //new
    public static final ResourceKey<DimensionType> CHEESE_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY,
            new ResourceLocation("cheesetopia"));

    protected static final DimensionType DEFAULT_OVERWORLD = create(OptionalLong.empty(), true, false,
            false, true, 1.0D, false, false, true, false,
            true, -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD.getName(), OVERWORLD_EFFECTS, 0.0F);
    protected static final DimensionType DEFAULT_NETHER = create(OptionalLong.of(18000L), false,
            true, true, false, 8.0D, false, true, false,
            true, false, 0, 256, 128, BlockTags.INFINIBURN_NETHER.getName(),
            NETHER_EFFECTS, 0.1F);
    protected static final DimensionType DEFAULT_END = create(OptionalLong.of(6000L), false,
            false, false, false, 1.0D, true, false, false,
            false, true, 0, 256, 256, BlockTags.INFINIBURN_END.getName(),
            END_EFFECTS, 0.0F);

    //new
    protected static final DimensionType DEFAULT_CHEESETOPIA = create(OptionalLong.of(12000L), true,
            false, false, true, 1.0D, false, false, true,
            false, true, -64, 384, 384,
            BlockTags.INFINIBURN_OVERWORLD.getName(), OVERWORLD_EFFECTS, 0.0F);

    public static final ResourceKey<DimensionType> OVERWORLD_CAVES_LOCATION = DimensionType.OVERWORLD_CAVES_LOCATION;
    //ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld_caves"));
    protected static final DimensionType DEFAULT_OVERWORLD_CAVES = create(OptionalLong.empty(), true, true, false, true, 1.0D, false, false, true, false, true, -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD.getName(), OVERWORLD_EFFECTS, 0.0F);

    //new
    //We pass the DIRECT_CODEC in here from DimensionType right now, but eventually we'll need the Cheesetopia
    // one so our dimension is included in the lists when referenced in the LevelStem
    public static final Codec<Supplier<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC);
    private final OptionalLong fixedTime;
    private final boolean hasSkylight;
    private final boolean hasCeiling;
    private final boolean ultraWarm;
    private final boolean natural;
    private final double coordinateScale;
    private final boolean createDragonFight;
    private final boolean piglinSafe;
    private final boolean bedWorks;
    private final boolean respawnAnchorWorks;
    private final boolean hasRaids;
    private final int minY;
    private final int height;
    private final int logicalHeight;
    private final ResourceLocation infiniburn;
    private final ResourceLocation effectsLocation;
    private final float ambientLight;
    private final transient float[] brightnessRamp;

    private static DataResult<DimensionType> guardY(DimensionType p_156719_) {
        if (p_156719_.height() < 16) {
            return DataResult.error("height has to be at least 16");
        } else if (p_156719_.minY() + p_156719_.height() > MAX_Y + 1) {
            return DataResult.error("min_y + height cannot be higher than: " + (MAX_Y + 1));
        } else if (p_156719_.logicalHeight() > p_156719_.height()) {
            return DataResult.error("logical_height cannot be higher than height");
        } else if (p_156719_.height() % 16 != 0) {
            return DataResult.error("height has to be multiple of 16");
        } else {
            return p_156719_.minY() % 16 != 0 ? DataResult.error("min_y has to be a multiple of 16") : DataResult.success(p_156719_);
        }
    }

    public CheesetopiaDimensionType(OptionalLong p_188296_, boolean p_188297_, boolean p_188298_, boolean p_188299_, boolean p_188300_, double p_188301_, boolean p_188302_, boolean p_188303_, boolean p_188304_, boolean p_188305_, boolean p_188306_, int p_188307_, int p_188308_, int p_188309_, ResourceLocation p_188310_, ResourceLocation p_188311_, float p_188312_) {
        this.fixedTime = p_188296_;
        this.hasSkylight = p_188297_;
        this.hasCeiling = p_188298_;
        this.ultraWarm = p_188299_;
        this.natural = p_188300_;
        this.coordinateScale = p_188301_;
        this.createDragonFight = p_188302_;
        this.piglinSafe = p_188303_;
        this.bedWorks = p_188304_;
        this.respawnAnchorWorks = p_188305_;
        this.hasRaids = p_188306_;
        this.minY = p_188307_;
        this.height = p_188308_;
        this.logicalHeight = p_188309_;
        this.infiniburn = p_188310_;
        this.effectsLocation = p_188311_;
        this.ambientLight = p_188312_;
        this.brightnessRamp = fillBrightnessRamp(p_188312_);
    }
    private static float[] fillBrightnessRamp(float p_63901_) {
        float[] afloat = new float[16];

        for(int i = 0; i <= 15; ++i) {
            float f = (float)i / 15.0F;
            float f1 = f / (4.0F - 3.0F * f);
            afloat[i] = Mth.lerp(p_63901_, f1, 1.0F);
        }

        return afloat;
    }
    @Deprecated
    public static DataResult<ResourceKey<Level>> parseLegacy(Dynamic<?> p_63912_) {
        Optional<Number> optional = p_63912_.asNumber().result();
        if (optional.isPresent()) {
            int i = optional.get().intValue();
            if (i == -1) {
                return DataResult.success(Level.NETHER);
            }

            if (i == 0) {
                return DataResult.success(Level.OVERWORLD);
            }

            if (i == 1) {
                return DataResult.success(Level.END);
            }
            //new
            //we can either add our custom dimension in here, or if we want it to be equal to the overworld, we could
            // do other math elsewhere. tag for this is //new mathHere
            if (i == 2 ){
                return DataResult.success(CheesetopiaDimensionLevelStem.CHEESETOPIA.);
            }
        }

        return Level.RESOURCE_KEY_CODEC.parse(p_63912_);
    }

    public static RegistryAccess registerBuiltin(RegistryAccess p_188316_) {
        WritableRegistry<DimensionType> writableregistry = p_188316_.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        writableregistry.register(OVERWORLD_LOCATION, DEFAULT_OVERWORLD, Lifecycle.stable());
        writableregistry.register(OVERWORLD_CAVES_LOCATION, DEFAULT_OVERWORLD_CAVES, Lifecycle.stable());
        writableregistry.register(NETHER_LOCATION, DEFAULT_NETHER, Lifecycle.stable());
        writableregistry.register(END_LOCATION, DEFAULT_END, Lifecycle.stable());
        //new
        //Registry call? Not sure what/why this is, just following pattern.
        writableregistry.register(CHEESE_LOCATION, DEFAULT_CHEESETOPIA, Lifecycle.stable());
        return p_188316_;
    }

    public static MappedRegistry<LevelStem> defaultDimensions(RegistryAccess p_188318_, long p_188319_) {
        return defaultDimensions(p_188318_, p_188319_, true);
    }


    public static MappedRegistry<LevelStem> defaultDimensions(RegistryAccess p_188321_, long p_188322_, boolean p_188323_) {
        MappedRegistry<LevelStem> mappedregistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        Registry<DimensionType> registry = p_188321_.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<Biome> registry1 = p_188321_.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<NoiseGeneratorSettings> registry2 = p_188321_.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<NormalNoise.NoiseParameters> registry3 = p_188321_.registryOrThrow(Registry.NOISE_REGISTRY);

        mappedregistry.register(LevelStem.NETHER, new LevelStem(() -> {
            return registry.getOrThrow(NETHER_LOCATION);
        }, new NoiseBasedChunkGenerator(registry3, MultiNoiseBiomeSource.Preset.NETHER.biomeSource(registry1, p_188323_), p_188322_, () -> {
            return registry2.getOrThrow(NoiseGeneratorSettings.NETHER);
        })), Lifecycle.stable());

        mappedregistry.register(LevelStem.END, new LevelStem(() -> {
            return registry.getOrThrow(END_LOCATION);
        }, new NoiseBasedChunkGenerator(registry3, new TheEndBiomeSource(registry1, p_188322_), p_188322_, () -> {
            return registry2.getOrThrow(NoiseGeneratorSettings.END);
        })), Lifecycle.stable());

        //new
        //A lot of following pattern here
        mappedregistry.register(CheesetopiaDimensionLevelStem.CHEESETOPIA, new LevelStem(() -> {
            return registry.getOrThrow(CHEESE_LOCATION);
        }, new NoiseBasedChunkGenerator(registry3, new BiomeSource(registry1, p_188322_) {
            @Override
            protected Codec<? extends BiomeSource> codec() {
                return null;
            }

            @Override
            public BiomeSource withSeed(long p_47916_) {
                return null;
            }

            @Override
            public Biome getNoiseBiome(int p_186735_, int p_186736_, int p_186737_, Climate.Sampler p_186738_) {
                return null;
            }
        }, p_188322_, () -> {
            return registry2.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
        })), Lifecycle.stable());

        return mappedregistry;
    }

    public static double getTeleportationScale(DimensionType p_63909_, DimensionType p_63910_) {
        double d0 = p_63909_.coordinateScale();
        double d1 = p_63910_.coordinateScale();
        return d0 / d1;
    }

    /** @deprecated */
    @Deprecated
    public String getFileSuffix() {
        return this.equalTo(DEFAULT_END) ? "_end" : "";
        //new
        //idk about this one
    }

    public static Path getStorageFolder(ResourceKey<Level> p_196976_, Path p_196977_) {
        if (p_196976_ == Level.OVERWORLD) {
            return p_196977_;
        } else if (p_196976_ == Level.END) {
            return p_196977_.resolve("DIM1");
        } else {
            return p_196976_ == Level.NETHER ? p_196977_.resolve("DIM-1") : p_196977_.resolve("dimensions").resolve(p_196976_.location().getNamespace()).resolve(p_196976_.location().getPath());
        }
    }

    public boolean hasSkyLight() {
        return this.hasSkylight;
    }
    public boolean hasCeiling() {
        return this.hasCeiling;
    }
    public boolean ultraWarm() {
        return this.ultraWarm;
    }
    public boolean natural() {
        return this.natural;
    }
    public double coordinateScale() {
        return this.coordinateScale;
    }
    public boolean piglinSafe() {
        return this.piglinSafe;
    }
    public boolean bedWorks() {
        return this.bedWorks;
    }
    public boolean respawnAnchorWorks() {
        return this.respawnAnchorWorks;
    }
    public boolean hasRaids() {
        return this.hasRaids;
    }
    public int minY() {
        return this.minY;
    }
    public int height() {
        return this.height;
    }
    public int logicalHeight() {
        return this.logicalHeight;
    }
    public boolean createDragonFight() {
        return this.createDragonFight;
    }
    public boolean hasFixedTime() {
        return this.fixedTime.isPresent();
    }

    public float timeOfDay(long p_63905_) {
        double d0 = Mth.frac((double)this.fixedTime.orElse(p_63905_) / 24000.0D - 0.25D);
        double d1 = 0.5D - Math.cos(d0 * Math.PI) / 2.0D;
        return (float)(d0 * 2.0D + d1) / 3.0F;
    }

    public int moonPhase(long p_63937_) {
        return (int)(p_63937_ / 24000L % 8L + 8L) % 8;
    }

    public float brightness(int p_63903_) {
        return this.brightnessRamp[p_63903_];
    }

    public Tag<Block> infiniburn() {
        Tag<Block> tag = BlockTags.getAllTags().getTag(this.infiniburn);
        return (Tag<Block>)(tag != null ? tag : BlockTags.INFINIBURN_OVERWORLD);
    }

    public ResourceLocation effectsLocation() {
        return this.effectsLocation;
    }

    //new, changed argument type from DimensionType to CheesetopiaDimensionType
    public boolean equalTo(CheesetopiaDimensionType p_63907_) {
        if (this == p_63907_) {
            return true;
        } else {
            return this.hasSkylight == p_63907_.hasSkylight && this.hasCeiling == p_63907_.hasCeiling && this.ultraWarm == p_63907_.ultraWarm && this.natural == p_63907_.natural && this.coordinateScale == p_63907_.coordinateScale && this.createDragonFight == p_63907_.createDragonFight && this.piglinSafe == p_63907_.piglinSafe && this.bedWorks == p_63907_.bedWorks && this.respawnAnchorWorks == p_63907_.respawnAnchorWorks && this.hasRaids == p_63907_.hasRaids && this.minY == p_63907_.minY && this.height == p_63907_.height && this.logicalHeight == p_63907_.logicalHeight && Float.compare(p_63907_.ambientLight, this.ambientLight) == 0 && this.fixedTime.equals(p_63907_.fixedTime) && this.infiniburn.equals(p_63907_.infiniburn) && this.effectsLocation.equals(p_63907_.effectsLocation);
        }
    }

}
