package me.dags.scraper.dynmap;

import me.dags.scraper.asset.AssetManager;
import me.dags.scraper.asset.AssetPath;
import me.dags.scraper.asset.model.Element;
import me.dags.scraper.asset.model.Model;
import me.dags.scraper.asset.model.ModelType;
import org.dynmap.modsupport.*;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */
public final class ModelRegistrar {

    private static final ModelRegistrar instance = new ModelRegistrar();

    private final Map<String, ModTextureDefinition> definitions = new HashMap<>();
    private final Map<AssetPath, TextureFile> textures = new HashMap<>();
    private Path texturePack;

    private ModelRegistrar() {}

    public static ModelRegistrar getInstance() {
        return instance;
    }

    public void setMCDir(File dir) {
        texturePack = dir.toPath().resolve("dynmap").resolve("texturepacks").resolve("standard");
    }

    public void publish() {
        Function<String, AssetPath> function = domain -> AssetPath.domain(domain).resolve("textures", "blocks");
        AssetManager.getInstance().extractAssets(function, texturePack);
        for (ModTextureDefinition definition : definitions.values()) {
            definition.getModelDefinition().publishDefinition();
            definition.publishDefinition();
        }
    }

    public void clear() {
        definitions.clear();
        textures.clear();
    }

    public void register(String domain, String name, int meta, ModelType type, Model model) {
        switch (type) {
            case DOOR:
                registerDoor(domain, name, model);
                break;
            case FENCE:
                registerWallFence(domain, name, model, WallFenceBlockModel.FenceType.FENCE);
            case PANE:
                registerPane(domain, name, model);
                break;
            case PLANT:
                registerPlant(domain, name, model);
                break;
            case WALL:
                registerWallFence(domain, name, model, WallFenceBlockModel.FenceType.WALL);
                break;
            default:
                registerCustom(domain, name, meta, model);
        }
    }

    private void registerCustom(String domain, String name, int meta, Model model) {
        ModTextureDefinition definition = getDefinition(domain);

        CuboidBlockModel blockModel = definition.getModelDefinition().addCuboidModel(name);
        constructCubeModel(blockModel, model);
        blockModel.setMetaValue(meta);

        BlockTextureRecord textureRecord = definition.addBlockTextureRecord(name);
        registerTextures(definition, textureRecord, model, true);
        textureRecord.setMetaValue(meta);
    }

    private void registerDoor(String domain, String name, Model model) {
        ModTextureDefinition definition = getDefinition(domain);
        definition.getModelDefinition().addDoorModel(name);
        BlockTextureRecord textureRecord = definition.addBlockTextureRecord(name);
        registerTextures(definition, textureRecord, model, true);
    }

    private void registerWallFence(String domain, String name, Model model, WallFenceBlockModel.FenceType type) {
        ModTextureDefinition definition = getDefinition(domain);
        definition.getModelDefinition().addWallFenceModel(name, type);
        BlockTextureRecord textureRecord = definition.addBlockTextureRecord(name);
        registerTextures(definition, textureRecord, model, true);
    }

    private void registerPane(String domain, String name, Model model) {
        ModTextureDefinition definition = getDefinition(domain);
        definition.getModelDefinition().addPaneModel(name);
        BlockTextureRecord textureRecord = definition.addBlockTextureRecord(name);
        registerTextures(definition, textureRecord, model, true);
    }

    private void registerPlant(String domain, String name, Model model) {
        ModTextureDefinition definition = getDefinition(domain);
        definition.getModelDefinition().addPlantModel(name);
        BlockTextureRecord textureRecord = definition.addBlockTextureRecord(name);
        registerTextures(definition, textureRecord, model, false);
    }

    private ModTextureDefinition getDefinition(String domain) {
        ModTextureDefinition definition = definitions.get(domain);
        if (definition == null) {
            definitions.put(domain, definition = ModSupportAPI.getAPI().getModTextureDefinition(domain, "1.0"));
        }
        return definition;
    }

    private TextureFile getTexture(ModTextureDefinition definition, AssetPath path) {
        TextureFile file = textures.get(path);
        if (file == null) {
            file = definition.registerTextureFile(path.getName(), path.withExtension(".png").toString());
            textures.put(path, file);
        }
        return file;
    }

    private void constructCubeModel(CuboidBlockModel blockModel, Model model) {
        for (Element element : model.getElements()) {
            double x1 = element.x1 / 16D;
            double y1 = element.y1 / 16D;
            double z1 = element.z1 / 16D;
            double x2 = element.x2 / 16D;
            double y2 = element.y2 / 16D;
            double z2 = element.z2 / 16D;
            double xmin = Math.min(x1, x2);
            double xmax = Math.max(x1, x2);
            double ymin = Math.min(y1, y2);
            double ymax = Math.max(y1, y2);
            double zmin = Math.min(z1, z2);
            double zmax = Math.max(z1, z2);
            blockModel.addCuboid(xmin, ymin, zmin, xmax, ymax, zmax, null);
        }
    }

    private void registerTextures(ModTextureDefinition definition, BlockTextureRecord textureRecord, Model model, boolean useFallback) {
        SideUtil counter = new SideUtil();

        for (Map.Entry<String, String> entry : model.getTextures()) {
            BlockSide side = SideUtil.fromName(entry.getKey());
            String icon = entry.getValue();

            if (icon.startsWith("#")) {
                icon = model.getTexture(icon.substring(1));
            }

            if (icon != null) {
                AssetPath path = AssetPath.of(icon, "textures/blocks");
                TextureFile textureFile = getTexture(definition, path);
                if (textureFile == null) {
                    throw new UnsupportedOperationException("Dynmap unable to register texture: " + path.withExtension(".png"));
                }
                counter.apply(textureRecord, textureFile, side);
                counter.recordSide(side, textureFile);
            }
        }

        if (useFallback && counter.hasFallback()) {
            TextureFile fallback = counter.getFallbackTexture();
            for (BlockSide side : counter.getMissingSides()) {
                counter.apply(textureRecord, fallback, side);
            }
        }
    }
}
