package anticope.clientminer;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "clientminer")
public class ClientMinerConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip(count = 2)
    boolean raycast = false;

    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean automine = true;

    @ConfigEntry.Gui.Tooltip
    public boolean snapBack = true;

    @ConfigEntry.Gui.Tooltip
    public float rotationSpeed = 0.4f;

    @ConfigEntry.Gui.Tooltip
    public boolean sound = true;
}
