package map;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.event.MouseInputListener;
import java.io.File;

public class MapaFactory {

    public static JXMapViewer criar() {
        JXMapViewer mapa = new JXMapViewer();

        final int max = 17;
        final String url = "https://tile.openstreetmap.org/";
        TileFactoryInfo info = new TileFactoryInfo(1, max - 2, max, 256, true, true, url, "z", "x", "y") {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                int z = max - zoom;
                return url + z + "/" + x + "/" + y + ".png";
            }
        };

        DefaultTileFactory fabrica = new DefaultTileFactory(info);
        fabrica.setThreadPoolSize(8);

        File pastaCache = new File(System.getProperty("user.home"), ".jxmapviewer2");
        fabrica.setLocalCache(new FileBasedLocalCache(pastaCache, false));

        mapa.setTileFactory(fabrica);
        mapa.setZoom(11);
        mapa.setAddressLocation(new GeoPosition(-29.67, -52.6));

        MouseInputListener arrastar = new PanMouseInputListener(mapa);
        mapa.addMouseListener(arrastar);
        mapa.addMouseMotionListener(arrastar);
        mapa.addMouseListener(new CenterMapListener(mapa));
        mapa.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapa));
        mapa.addKeyListener(new PanKeyListener(mapa));

        return mapa;
    }
}
