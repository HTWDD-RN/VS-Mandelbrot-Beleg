import java.io.Serializable;

/**
 * Bündelt alle relevanten Zoom-/Bildparameter für einen RMI-Aufruf.
 */
public class ZoomParameters implements Serializable {
    public final double centerRe, centerIm;
    public final double zoom;
    public final int    maxIter;
    public final int    imgW, imgH;

    public ZoomParameters(
        double centerRe, double centerIm,
        double zoom, int maxIter,
        int imgW, int imgH
    ) {
        this.centerRe = centerRe;
        this.centerIm = centerIm;
        this.zoom     = zoom;
        this.maxIter  = maxIter;
        this.imgW     = imgW;
        this.imgH     = imgH;
    }
}
