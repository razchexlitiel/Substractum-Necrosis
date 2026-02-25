package razchexlitiel.cim.client.gecko.block.rotation;


import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import razchexlitiel.cim.block.entity.rotation.WindGenFlugerBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class WindGenFlugerRenderer extends GeoBlockRenderer<WindGenFlugerBlockEntity> {
    public WindGenFlugerRenderer(BlockEntityRendererProvider.Context context) {
        super(new WindGenFlugerModel());
    }
}