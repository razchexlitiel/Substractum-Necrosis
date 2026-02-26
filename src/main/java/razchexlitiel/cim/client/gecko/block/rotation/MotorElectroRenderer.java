package razchexlitiel.cim.client.gecko.block.rotation;


import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import razchexlitiel.cim.block.entity.rotation.MotorElectroBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class MotorElectroRenderer extends GeoBlockRenderer<MotorElectroBlockEntity> {
    public MotorElectroRenderer(BlockEntityRendererProvider.Context context) {
        super(new MotorElectroModel());
    }
}