package ryey.camcov;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class CamTileService extends TileService {
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateState(OverlayService.isRunning());
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        if (isLocked()) {
            unlockAndRun(new Runnable() {
                @Override
                public void run() {
                    toggle();
                }
            });
        }
        else {
            toggle();
        }
    }

    private void toggle() {
        try {
            updateState(!OverlayService.isRunning());
            OverlayService.toggle(this);
        } finally {
            updateState(OverlayService.isRunning());
        }
    }

    private void updateState(boolean isRunning) {
        Tile tile = getQsTile();
        tile.setState(isRunning ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }
}
