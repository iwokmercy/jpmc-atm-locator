package com.example.mjexco.jpmcatmlocator;

import com.example.mjexco.jpmcatmlocator.domain.Location;
import com.example.mjexco.jpmcatmlocator.service.request.AtmLocationService;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AtmLocationServiceTest {

    @Test
    public void testGetInstance(){
        assertNotNull("getInstance returns null instance", AtmLocationService.getInstance());
    }

    @Test
    /**
     * Attempt to retrieve atm locations before making the service call.
     * Should return empty array list
     */
    public void testGetLocations(){
        List<Location> locations = AtmLocationService.getInstance().getLocations();
        assertEquals(0, locations.size());
    }
}
