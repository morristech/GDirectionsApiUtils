/**
 * <ul>
 * <li>GoogleMapSample</li>
 * <li>com.android2ee.formation.librairies.google.map.utils.direction</li>
 * <li>12 sept. 2013</li>
 * <p>
 * <li>======================================================</li>
 * <p>
 * <li>Projet : Mathias Seguy Project</li>
 * <li>Produit par MSE.</li>
 * <p>
 * /**
 * <ul>
 * Android Tutorial, An <strong>Android2EE</strong>'s project.</br>
 * Produced by <strong>Dr. Mathias SEGUY</strong>.</br>
 * Delivered by <strong>http://android2ee.com/</strong></br>
 * Belongs to <strong>Mathias Seguy</strong></br>
 * ***************************************************************************************************************</br>
 * This code is free for any usage except training and can't be distribute.</br>
 * The distribution is reserved to the site <strong>http://android2ee.com</strong>.</br>
 * The intelectual property belongs to <strong>Mathias Seguy</strong>.</br>
 * <em>http://mathias-seguy.developpez.com/</em></br> </br>
 * <p>
 * *****************************************************************************************************************</br>
 * Ce code est libre de toute utilisation mais n'est pas distribuable.</br>
 * Sa distribution est reservée au site <strong>http://android2ee.com</strong>.</br>
 * Sa propriété intellectuelle appartient à <strong>Mathias Seguy</strong>.</br>
 * <em>http://mathias-seguy.developpez.com/</em></br> </br>
 * *****************************************************************************************************************</br>
 */
package com.android2ee.formation.librairies.google.map.utils.direction;

import android.graphics.Color;
import com.android2ee.formation.librairies.google.map.utils.direction.com.IGDirectionServer;
import com.android2ee.formation.librairies.google.map.utils.direction.com.RetrofitBuilder;
import com.android2ee.formation.librairies.google.map.utils.direction.model.GDColor;
import com.android2ee.formation.librairies.google.map.utils.direction.model.GDLegs;
import com.android2ee.formation.librairies.google.map.utils.direction.model.GDPath;
import com.android2ee.formation.librairies.google.map.utils.direction.model.GDPoint;
import com.android2ee.formation.librairies.google.map.utils.direction.model.GDirection;
import com.android2ee.formation.librairies.google.map.utils.direction.util.GDirectionData;
import com.android2ee.formation.librairies.google.map.utils.direction.util.GDirectionMapsOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Mathias Seguy (Android2EE)
 * @goals This class aims to make a layer over the Direction Api.
 * To have a Google Direction and Draw it You just have to:
 * <ul>
 * <li>Implements the DCACallBack and its method public void onDirectionLoaded(GDirection
 * direction)</li>
 * <li>Then you call GDirectionApiUtils.getDirection(DCACallBack, start point, end point,
 * GDirectionsApiUtils.MODE_***); When the GDirection is build, the DCACallBack is called
 * giving you the GDirection you are waiting for.</li>
 * <li>Then you can handle yourself the GDirection or you can call drawGDirection(GDirection
 * direction, GoogleMap map)</li>
 * </ul>
 * d
 * public class MainActivity extends ActionBarActivity implements DCACallBack {</br>
 * private void getDirections(LatLng point) {</br>
 * GDirectionsApiUtils.getDirection(this, mDeviceLatlong, point,
 * GDirectionsApiUtils.MODE_WALKING);</br>
 * }</br>
 * public void onDirectionLoaded(GDirection direction) {</br>
 * Log.e("MainActivity", "onDirectionLoaded : Draw GDirections Called with path " +
 * direction);</br>
 * // Display the direction or use the DirectionsApiUtils</br>
 * GDirectionsApiUtils.drawGDirection(direction, mMap);</br>
 * }
 */
public class GDirectionsApiUtils {
    /***********************************************************
     *  Attributes
     **********************************************************/
    private static final String TAG = "GDirectionsApiUtils";

    private static WeakReference<DCACallBack> callbackWeakRef;

    private static LatLngBounds.Builder latLngBuilder;
    private static LatLngBounds bounds;

    /******************************************************************************************/
    /** Public Method **************************************************************************/
    /******************************************************************************************/

    /**
     * Draw on the given map the given GDirection object
     *
     * @param direction The google direction to draw
     * @param map       The map to draw on
     */
    public static void drawGDirection(GDirection direction, GoogleMap map) {
        drawGDirection(direction, map, null);
    }


    /**
     * Draw on the given map the given GDirection object
     *
     * @param direction   The google direction to draw
     * @param map         The map to draw on
     * @param mapsOptions mapsOptions to draw on google maps
     */
    public static void drawGDirection(GDirection direction, GoogleMap map, GDirectionMapsOptions mapsOptions) {
        // The polyline option to create polyline
        PolylineOptions lineOptions = null;
        // index of GDPoint within the current GDPath
        int i = 0;
        // index of the current GDPath
        int pathIndex = 0;
        // index of the current GDLegs
        int legsIndex = 0;
        ArrayList<GDColor> colors = null;
        if (mapsOptions != null) {
            colors = mapsOptions.getColors();
        }

        IGDFormatter formatter = null;
        if (mapsOptions != null) {
            formatter = mapsOptions.getFormatter();
        }

        // Browse the directions' legs and then the leg's paths
        for (GDLegs legs : direction.getLegsList()) {
            for (GDPath path : legs.getPathsList()) {

                if (pathIndex == 0 || pathIndex == legsIndex)

                    // Create the polyline
                    if (mapsOptions != null) {
                        lineOptions = mapsOptions.getPolylineOptions();
                    } else {
                        lineOptions = new PolylineOptions();
                        // A 5 width Polyline please
                        lineOptions.width(5);
                        // color options (alternating green/blue path)
                        if (legsIndex % 2 == 0) {
                            lineOptions.color(Color.GREEN);
                        } else {
                            lineOptions.color(Color.BLUE);
                        }
                    }
                // manage indexes
                i = 0;
                pathIndex++;
                // browse the GDPoint that define the path
                for (GDPoint point : path.getPath()) {
                    i++;
                    // Add the point to the polyline
                    lineOptions.add(point.getLatLng());
                    // Mark the last GDPoint of the path with a HUE_AZURE marker
                    if (i == path.getPath().size() - 1) {
                        // create marker
                        Marker marker = map.addMarker(new MarkerOptions().position(point.getLatLng())
                                .title(formatter != null ? formatter.getTitle(path) : "Step " + i)
                                .snippet(formatter != null ? formatter.getSnippet(path) : "Step " + i)
                                .icon(((colors != null && colors.size() > 0) ?
                                        BitmapDescriptorFactory.defaultMarker(colors.get(legsIndex % colors.size()).colorPin) :
                                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
                        // if we have a custom snippet call contents
                        if (formatter != null && formatter.isInfoWindows()) {
                            formatter.setContents(marker, direction, legs, path);
                            marker.showInfoWindow();
                        }
                    }
                }

                // Override polyline color
                if (colors != null && colors.size() > 0) {
                    lineOptions.color(colors.get(legsIndex % colors.size()).colorLine);
                }
                // Drawing polyline in the Google Map for the route
                map.addPolyline(lineOptions);

            }
            legsIndex++;
        }
    }

    /**
     * Draw on the given map the given GDirection object
     *
     * @param direction   The google direction to draw
     * @param map         The map to draw on
     * @param mapsOptions mapsOptions to draw on google maps
     */
    public static void drawGDirectionWithoutPathMarker(GDirection direction, GoogleMap map, GDirectionMapsOptions mapsOptions) {
        List<LatLng> pathList = new ArrayList<>();
        // The polyline option to create polyline
        PolylineOptions lineOptions = null;
        int legsIndex = 0;
        int maxDotsDisplayed;
        ArrayList<GDColor> colors = null;
        if (mapsOptions != null) {
            colors = mapsOptions.getColors();
        }
        //avoid out of memory
        if (mapsOptions != null) {
            maxDotsDisplayed = mapsOptions.getMaxDotsToDisplay();
        } else {
            maxDotsDisplayed = GDirectionMapsOptions.MAX_DOTS_DISPLAYED_DEFAULT;
        }
        latLngBuilder = new LatLngBounds.Builder();
        GDirection reducedDirection = reduce(direction, maxDotsDisplayed);

        // Browse the directions' legs and then the leg's paths
        for (GDLegs legs : direction.getLegsList()) {
            for (GDPath path : legs.getPathsList()) {
                // Create the polyline
                if (mapsOptions != null) {
                    lineOptions = mapsOptions.getPolylineOptions();
                } else {
                    lineOptions = new PolylineOptions();
                    // A 5 width Polyline please
                    lineOptions.width(5);
                    // color options (alternating green/blue path)
                    if (legsIndex % 2 == 0) {
                        lineOptions.color(Color.GREEN);
                    } else {
                        lineOptions.color(Color.BLUE);
                    }
                }
                // browse the GDPoint that define the path
                for (GDPoint point : path.getPath()) {
                    pathList.add(point.getLatLng());
                    latLngBuilder.include(point.getLatLng());
                }

                // Override polyline color
                if (colors != null && colors.size() > 0) {
                    lineOptions.color(colors.get(legsIndex % colors.size()).colorLine);
                }
                legsIndex++;
            }
        }

        bounds = latLngBuilder.build();

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        map.addPolyline(lineOptions.addAll(pathList));

        reducedDirection = null;
    }

    /**
     * The goal of this method is to reduce the number of point to draw
     * according to the value of MapOtion.maxDotsDisplayed
     *
     * @param gDir
     */
    public static GDirection reduce(GDirection gDir, int maxDotsDisplayed) {
        int gWeight = gDir.getWeight();
        int skippedDotsStep = gWeight / maxDotsDisplayed;
        int currentStepIndex = 0;
        ArrayList<GDLegs> currentLegList = new ArrayList<>();
        GDLegs currentLeg;
        ArrayList<GDPath> currentPathList;
        GDPath currentPath;
        ArrayList<GDPoint> currentPointList;

        int currentPathIndex = 1;

        for (GDLegs legs : gDir.getLegsList()) {
            currentPathList = new ArrayList<>(legs.getPathsList().size());

            for (GDPath path : legs.getPathsList()) {
                currentPointList = new ArrayList<>(path.getWeight() / skippedDotsStep);
                currentPointList.add(path.getPath().get(0));

                //To include the start point in the Path List
                if( currentPathIndex == 1 ) {
                    currentPointList.add(path.getPath().get(0));
                }

                for (GDPoint point : path.getPath()) {
                    currentStepIndex++;
                    if (currentStepIndex % skippedDotsStep == 0) {
                        currentPointList.add(point);
                    }
                }
                currentPathIndex++;

                //To include the end point in the Path List
                if( currentPathIndex == legs.getPathsList().size()) {
                    currentPointList.add(path.getPath().get(path.getPath().size() - 1));
                }

                currentPath = new GDPath(currentPointList);
                currentPathList.add(currentPath);
            }
            currentLeg = new GDLegs(currentPathList);
            currentLegList.add(currentLeg);
        }
        GDirection returnedGDir = new GDirection(currentLegList);
        return returnedGDir;
    }

    /**
     * Find the direction between two points on the maps (direction is the path to follow to go from
     * start to end points)
     *
     * @param callback The DCACallBack to prevent when data have been retrieve and built. It will receive
     *                 a GDirection
     * @param data     builder GDirection
     */
    public static void getDirection(final DCACallBack callback, GDirectionData data) {
        callbackWeakRef = new WeakReference<DCACallBack>(callback);
        loadGDirections(data);
    }

    /**
     * Make the real job of loading directions
     *
     * @param data
     */
    private static void loadGDirections(GDirectionData data) {
        IGDirectionServer gDirectionServer = RetrofitBuilder.getBaseRetrofit().create(IGDirectionServer.class);
        Call<List<GDirection>> call = gDirectionServer.getGDirections(
                data.getStart().latitude + "," + data.getStart().longitude,
                data.getEnd().latitude + "," + data.getEnd().longitude,
                false,
                data.getMode().toString(),
                data.getWaypoints(),
                data.isAlternative(),
                data.getAvoid().toString(),
                data.getLanguage(),
                data.getUs().toString(),
                data.getRegion(),
                data.getDeparture_time(),
                data.getArrival_time()
        );
        call.enqueue(new Callback<List<GDirection>>() {
            @Override
            public void onResponse(Call<List<GDirection>> call, Response<List<GDirection>> response) {
                if (callbackWeakRef.get() != null) {
                    if (response.code() == 200) {
                        callbackWeakRef.get().onDirectionLoaded(response.body());
                    } else {
                        callbackWeakRef.get().onDirectionLoadedFailure();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<GDirection>> call, Throwable t) {
                if (callbackWeakRef.get() != null) {
                    callbackWeakRef.get().onDirectionLoadedFailure();
                }
            }
        });
    }

}
