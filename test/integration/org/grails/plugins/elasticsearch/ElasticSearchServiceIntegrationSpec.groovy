package org.grails.plugins.elasticsearch

import grails.plugin.spock.IntegrationSpec
import org.apache.log4j.Logger
import test.Product

class ElasticSearchServiceIntegrationSpec extends IntegrationSpec {
    def elasticSearchService
    def elasticSearchAdminService
    def elasticSearchContextHolder
    def elasticSearchHelper
    private static final Logger LOG = Logger.getLogger(this);

    def setup() {
        // Make sure the indices are cleaned
        println "cleaning indices"
        elasticSearchAdminService.deleteIndex()
        elasticSearchAdminService.refresh()
    }

    def "Index a domain object"() {
        given:
        def product = new Product(name: "myTestProduct")
        product.save()

        when:
        elasticSearchService.index(product)
        elasticSearchAdminService.refresh() // Ensure the latest operations have been exposed on the ES instance

        then:
        elasticSearchService.search("myTestProduct", [indices: Product, types: Product]).total == 1
    }

    def "Unindex method delete index from ES"() {
        given:
        def product = new Product(name: "myTestProduct")
        product.save()

        when:
        elasticSearchService.index(product)
        elasticSearchAdminService.refresh() // Ensure the latest operations have been exposed on the ES instance

        and:
        elasticSearchService.search("myTestProduct", [indices: Product, types: Product]).total == 1

        then:
        elasticSearchService.unindex(product)
        elasticSearchAdminService.refresh()

        and:
        elasticSearchService.search("myTestProduct", [indices: Product, types: Product]).total == 0
    }

    def "Indexing multiple time the same object update the corresponding ES entry"() {
        given:
        def product = new Product(name: "myTestProduct")
        product.save()

        when:
        elasticSearchService.index(product)
        elasticSearchAdminService.refresh()

        then:
        elasticSearchService.search("myTestProduct", [indices: Product, types: Product]).total == 1

        when:
        product.name = "newProductName"
        elasticSearchService.index(product)
        elasticSearchAdminService.refresh()

        then:
        elasticSearchService.search("myTestProduct", [indices: Product, types: Product]).total == 0

        and:
        def result = elasticSearchService.search(product.name, [indices: Product, types: Product])
        result.total == 1
        result.searchResults[0].name == product.name

    }

    void "a date value should be marshalled and demarshalled correctly"() {
        def date = new Date()
        given:
        def product = new Product(
            name: 'product with date value',
            date: date
        ).save(failOnError: true)

        elasticSearchService.index(product)
        elasticSearchAdminService.refresh()

        when:
        def result = elasticSearchService.search(product.name, [indices: Product, types: Product])

        then:
        result.total == 1
        result.searchResults[0].name == product.name
        result.searchResults[0].date == product.date
    }

/*
    void "a geo point location is marshalled and demarshalled correctly"() {
        given:
        def location = new GeoPoint(
            lat: 53.00,
            lon: 10.00
        ).save(failOnError: true)
        def building = new Building(
            location: location
        ).save(failOnError: true)

        elasticSearchService.index(building)
        elasticSearchAdminService.refresh()

        when:
        def result = elasticSearchService.search("${building.location}", [indices: Building, types: Building])

*/
/*
elasticSearchService.search([indices: Building, type: Building], query) {
geo_distance: {
distance: "50km"
"building.location": [ lat: 53.63, lon: 9.8 ]
}
}
*//*


        then:
        elasticSearchHelper.client.admin().indices()

        result.total == 1
        result.searchResults[0].location == location
    }
*/

    void "searching with filtered query"() {
        given: "some products"
        def product1 = new Product(name: "wurst", price: 2.00)
        product1.save()

        elasticSearchService.index(product1)
        elasticSearchAdminService.refresh()

        when: "that a range filter find the product"
        def searchResult = elasticSearchService.search(null as Closure, {
            range {
                "price"(gte: 1, lte: 3)
            }
        })
        then: "the result should be product1"
        1 == searchResult.searchResults.size()
        product1.name == searchResult.searchResults[0].name


    }
}
