package com.paklog.lastmile.domain.service;

import com.paklog.lastmile.domain.aggregate.DeliveryRoute;
import com.paklog.lastmile.domain.aggregate.DeliveryStop;
import com.paklog.lastmile.domain.aggregate.Vehicle;
import com.paklog.lastmile.domain.valueobject.GPSCoordinates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Vehicle Routing Problem (VRP) solver with time windows constraint
 * Uses a greedy nearest-neighbor heuristic with 2-opt local search improvement
 */
@Slf4j
@Service
public class RouteOptimizationService {

    private static final double TRAFFIC_FACTOR = 1.2; // 20% traffic overhead
    private static final int MAX_OPTIMIZATION_ITERATIONS = 100;

    /**
     * Optimizes route using VRP with time windows
     */
    public DeliveryRoute optimizeRoute(DeliveryRoute route, Vehicle vehicle) {
        log.info("Optimizing route {} with {} stops", route.getId(), route.getStops().size());

        List<DeliveryStop> stops = new ArrayList<>(route.getStops());

        if (stops.isEmpty()) {
            return route;
        }

        // Phase 1: Initial solution using nearest neighbor heuristic
        List<DeliveryStop> optimizedStops = nearestNeighborWithTimeWindows(
            stops,
            vehicle.getCurrentLocation() != null ? vehicle.getCurrentLocation() : vehicle.getHomeBase()
        );

        // Phase 2: Improve solution using 2-opt local search
        optimizedStops = twoOptImprovement(optimizedStops);

        // Calculate total distance and score
        double totalDistance = calculateTotalDistance(optimizedStops, vehicle.getHomeBase());
        int score = calculateOptimizationScore(optimizedStops, totalDistance);

        // Update route with optimized stops
        route.optimize(optimizedStops, totalDistance, score);

        log.info("Route optimized: distance={}km, score={}", totalDistance, score);

        return route;
    }

    /**
     * Nearest Neighbor heuristic respecting time windows
     */
    private List<DeliveryStop> nearestNeighborWithTimeWindows(List<DeliveryStop> stops, GPSCoordinates startLocation) {
        List<DeliveryStop> unvisited = new ArrayList<>(stops);
        List<DeliveryStop> route = new ArrayList<>();

        GPSCoordinates currentLocation = startLocation;
        Instant currentTime = Instant.now();

        while (!unvisited.isEmpty()) {
            DeliveryStop nearest = findNearestFeasibleStop(currentLocation, currentTime, unvisited);

            if (nearest == null) {
                // No feasible stop found - take the nearest one anyway
                nearest = findNearestStop(currentLocation, unvisited);
            }

            route.add(nearest);
            unvisited.remove(nearest);

            // Update current position and time
            currentLocation = nearest.getCoordinates();
            double travelTime = calculateTravelTime(currentLocation, nearest.getCoordinates());
            currentTime = currentTime.plus((long) travelTime, ChronoUnit.MINUTES)
                                    .plus(nearest.getEstimatedDurationMinutes(), ChronoUnit.MINUTES);

            // Update stop ETA
            nearest.updateETA(currentTime);
        }

        // Re-sequence stops
        for (int i = 0; i < route.size(); i++) {
            route.get(i).setSequence(i + 1);
        }

        return route;
    }

    /**
     * 2-opt local search for route improvement
     */
    private List<DeliveryStop> twoOptImprovement(List<DeliveryStop> route) {
        List<DeliveryStop> best = new ArrayList<>(route);
        double bestDistance = calculateTotalDistance(best, null);

        boolean improved = true;
        int iteration = 0;

        while (improved && iteration < MAX_OPTIMIZATION_ITERATIONS) {
            improved = false;
            iteration++;

            for (int i = 1; i < route.size() - 1; i++) {
                for (int j = i + 1; j < route.size(); j++) {
                    // Try reversing the segment between i and j
                    List<DeliveryStop> newRoute = twoOptSwap(best, i, j);
                    double newDistance = calculateTotalDistance(newRoute, null);

                    if (newDistance < bestDistance && isTimeWindowFeasible(newRoute)) {
                        best = newRoute;
                        bestDistance = newDistance;
                        improved = true;
                    }
                }
            }
        }

        log.debug("2-opt completed in {} iterations, distance improved to {}km", iteration, bestDistance);

        return best;
    }

    /**
     * Performs a 2-opt swap
     */
    private List<DeliveryStop> twoOptSwap(List<DeliveryStop> route, int i, int j) {
        List<DeliveryStop> newRoute = new ArrayList<>();

        // Add elements before index i
        newRoute.addAll(route.subList(0, i));

        // Add reversed segment from i to j
        List<DeliveryStop> reversed = new ArrayList<>(route.subList(i, j + 1));
        Collections.reverse(reversed);
        newRoute.addAll(reversed);

        // Add remaining elements after j
        if (j + 1 < route.size()) {
            newRoute.addAll(route.subList(j + 1, route.size()));
        }

        return newRoute;
    }

    /**
     * Find nearest stop that respects time windows
     */
    private DeliveryStop findNearestFeasibleStop(GPSCoordinates from, Instant currentTime,
                                                  List<DeliveryStop> candidates) {
        return candidates.stream()
            .filter(stop -> {
                double travelTime = calculateTravelTime(from, stop.getCoordinates());
                Instant arrivalTime = currentTime.plus((long) travelTime, ChronoUnit.MINUTES);
                return stop.getWindow() == null || stop.isWithinWindow(arrivalTime) ||
                       arrivalTime.isBefore(stop.getWindow().getEndTime());
            })
            .min(Comparator.comparingDouble(stop -> from.distanceTo(stop.getCoordinates())))
            .orElse(null);
    }

    /**
     * Find nearest stop without time window constraint
     */
    private DeliveryStop findNearestStop(GPSCoordinates from, List<DeliveryStop> candidates) {
        return candidates.stream()
            .min(Comparator.comparingDouble(stop -> from.distanceTo(stop.getCoordinates())))
            .orElseThrow(() -> new IllegalStateException("No stops available"));
    }

    /**
     * Check if route satisfies all time windows
     */
    private boolean isTimeWindowFeasible(List<DeliveryStop> route) {
        Instant currentTime = Instant.now();

        for (DeliveryStop stop : route) {
            if (stop.getWindow() != null) {
                double travelTime = calculateTravelTime(
                    route.indexOf(stop) > 0 ?
                        route.get(route.indexOf(stop) - 1).getCoordinates() :
                        stop.getCoordinates(),
                    stop.getCoordinates()
                );

                Instant arrivalTime = currentTime.plus((long) travelTime, ChronoUnit.MINUTES);

                if (arrivalTime.isAfter(stop.getWindow().getEndTime())) {
                    return false;
                }

                currentTime = arrivalTime.plus(stop.getEstimatedDurationMinutes(), ChronoUnit.MINUTES);
            }
        }

        return true;
    }

    /**
     * Calculate total route distance
     */
    private double calculateTotalDistance(List<DeliveryStop> stops, GPSCoordinates returnLocation) {
        if (stops.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;

        for (int i = 0; i < stops.size() - 1; i++) {
            total += stops.get(i).getCoordinates().distanceTo(stops.get(i + 1).getCoordinates());
        }

        // Add return to base if specified
        if (returnLocation != null && !stops.isEmpty()) {
            total += stops.get(stops.size() - 1).getCoordinates().distanceTo(returnLocation);
        }

        return total;
    }

    /**
     * Calculate travel time in minutes considering traffic
     */
    private double calculateTravelTime(GPSCoordinates from, GPSCoordinates to) {
        double distanceKm = from.distanceTo(to);
        // Assume 24 km/h average urban speed, apply traffic factor
        return (distanceKm / 24.0) * 60.0 * TRAFFIC_FACTOR;
    }

    /**
     * Calculate optimization score (0-100)
     * Higher is better
     */
    private int calculateOptimizationScore(List<DeliveryStop> stops, double totalDistance) {
        // Base score on distance efficiency
        double avgStopDistance = stops.size() > 1 ? totalDistance / stops.size() : totalDistance;

        // Ideal avg distance per stop is 2-3 km
        double distanceScore = Math.max(0, 100 - (avgStopDistance - 2.5) * 20);

        // Time window compliance score
        long windowViolations = stops.stream()
            .filter(stop -> stop.getWindow() != null && stop.getEstimatedArrival() != null)
            .filter(stop -> !stop.isWithinWindow(stop.getEstimatedArrival()))
            .count();

        double windowScore = Math.max(0, 100 - (windowViolations * 10));

        // Combined score
        return (int) ((distanceScore * 0.6) + (windowScore * 0.4));
    }

    /**
     * Dynamic route re-optimization for new urgent stops
     */
    public DeliveryRoute insertUrgentStop(DeliveryRoute route, DeliveryStop urgentStop) {
        log.info("Inserting urgent stop into route {}", route.getId());

        List<DeliveryStop> stops = new ArrayList<>(route.getStops());
        DeliveryStop nextStop = route.getNextStop();

        if (nextStop == null) {
            // Route is complete, create new one
            stops.add(urgentStop);
        } else {
            // Find best insertion point
            int bestPosition = findBestInsertionPosition(stops, urgentStop, nextStop);
            stops.add(bestPosition, urgentStop);
        }

        // Re-optimize with new stop
        return optimizeRoute(route, null);
    }

    /**
     * Find optimal position to insert a new stop
     */
    private int findBestInsertionPosition(List<DeliveryStop> stops, DeliveryStop newStop,
                                         DeliveryStop currentStop) {
        int currentIndex = stops.indexOf(currentStop);
        int bestPosition = currentIndex;
        double minCost = Double.MAX_VALUE;

        // Try inserting at each position after current
        for (int i = currentIndex; i < stops.size(); i++) {
            double insertionCost = calculateInsertionCost(stops, newStop, i);

            if (insertionCost < minCost) {
                minCost = insertionCost;
                bestPosition = i;
            }
        }

        return bestPosition;
    }

    /**
     * Calculate cost of inserting stop at given position
     */
    private double calculateInsertionCost(List<DeliveryStop> stops, DeliveryStop newStop, int position) {
        double cost = 0.0;

        if (position > 0) {
            cost += stops.get(position - 1).getCoordinates().distanceTo(newStop.getCoordinates());
        }

        if (position < stops.size()) {
            cost += newStop.getCoordinates().distanceTo(stops.get(position).getCoordinates());
        }

        if (position > 0 && position < stops.size()) {
            // Subtract the original direct distance
            cost -= stops.get(position - 1).getCoordinates()
                        .distanceTo(stops.get(position).getCoordinates());
        }

        return cost;
    }
}
