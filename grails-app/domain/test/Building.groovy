package test

class Building {

    String name
    GeoPoint location

    static searchable = {
        location geoPoint: true
    }
}