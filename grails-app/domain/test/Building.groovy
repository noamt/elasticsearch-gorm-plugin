package test

class Building {

    GeoPoint location

    static searchable = {
        location geoPoint: true
    }
}