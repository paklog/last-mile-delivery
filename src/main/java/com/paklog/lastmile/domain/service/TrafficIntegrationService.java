package com.paklog.lastmile.domain.service;

import com.paklog.lastmile.domain.valueobject.GPSCoordinates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Integrates with external traffic APIs to get real-time traffic data
 * This is a stub implementation - in production would call Google Maps API, HERE, etc.
 */
@Slf4j
@Service
public class TrafficIntegrationService {

    private final Random random = new Random();

    /**
     * Get current traffic delay for a route segment
     * @return delay in minutes
     */
    public double getTrafficDelay(GPSCoordinates from, GPSCoordinates to) {
        // Stub implementation - simulates traffic conditions
        double distance = from.distanceTo(to);

        // Base travel time at 24 km/h
        double baseTravelTime = (distance / 24.0) * 60.0;

        // Random traffic factor between 1.0 (no traffic) and 2.0 (heavy traffic)
        double trafficFactor = 1.0 + random.nextDouble();

        double delayMinutes = baseTravelTime * (trafficFactor - 1.0);

        log.debug("Traffic delay for {:.2f}km: {:.1f} minutes", distance, delayMinutes);

        return delayMinutes;
    }

    /**
     * Check if there are incidents on the route
     */
    public boolean hasTrafficIncidents(GPSCoordinates from, GPSCoordinates to) {
        // Stub: 10% chance of incidents
        return random.nextDouble() < 0.1;
    }

    /**
     * Get estimated time of arrival considering current traffic
     */
    public int getETAMinutes(GPSCoordinates from, GPSCoordinates to) {
        double distance = from.distanceTo(to);
        double baseTravelTime = (distance / 24.0) * 60.0;
        double trafficDelay = getTrafficDelay(from, to);

        return (int) Math.ceil(baseTravelTime + trafficDelay);
    }

    /**
     * Get traffic congestion level (0-100)
     */
    public int getCongestionLevel(GPSCoordinates location) {
        // Stub: random congestion level
        return random.nextInt(101);
    }
}
