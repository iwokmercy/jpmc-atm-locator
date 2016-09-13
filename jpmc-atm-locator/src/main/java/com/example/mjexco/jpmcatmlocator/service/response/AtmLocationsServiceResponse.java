package com.example.mjexco.jpmcatmlocator.service.response;


import com.example.mjexco.jpmcatmlocator.domain.Errors;
import com.example.mjexco.jpmcatmlocator.domain.Location;

import java.util.ArrayList;
import java.util.List;

public class AtmLocationsServiceResponse {
    private List<Errors> errors = new ArrayList<>();
    private List<Location> locations = new ArrayList<>();

    /**
     *
     * @return
     * The errors
     */
    public List<Errors> getErrors() {
        return errors;
    }

    /**
     *
     * @param errors
     * The errors
     */
    public void setErrors(List<Errors> errors) {
        this.errors = errors;
    }

    /**
     *
     * @return
     * The locations
     */
    public List<Location> getLocations() {
        return locations;
    }

    /**
     *
     * @param locations
     * The locations
     */
    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public boolean hasErrors(){
        if(errors.size() == 0){
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AtmLocationsServiceResponse{" +
                "errors=" + errors +
                ", locations=" + locations +
                '}';
    }
}
