package xy177.combatrolantique.core;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("CombatRolAntique")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({"xy177.combatrolantique.core."})
public final class RollLoadingPlugin implements IFMLLoadingPlugin {
    @Override public String[] getASMTransformerClass() { return new String[]{"xy177.combatrolantique.core.RollTransformer"}; }
    @Override public String getModContainerClass() { return null; }
    @Override public String getSetupClass() { return null; }
    @Override public void injectData(Map<String,Object> data) { }
    @Override public String getAccessTransformerClass() { return null; }
}
