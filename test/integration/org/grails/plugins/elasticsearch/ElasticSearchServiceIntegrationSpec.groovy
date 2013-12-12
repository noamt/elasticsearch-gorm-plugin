package org.grails.plugins.elasticsearch

import grails.plugin.spock.IntegrationSpec
import org.apache.log4j.Logger
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequestBuilder
import org.elasticsearch.client.AdminClient
import org.elasticsearch.client.ClusterAdminClient
import org.elasticsearch.cluster.ClusterState
import org.elasticsearch.cluster.metadata.IndexMetaData
import org.elasticsearch.cluster.metadata.MappingMetaData
import test.Building
import test.GeoPoint
import test.Product

class ElasticSearchServiceIntegrationSpec extends IntegrationSpec {

    def elasticSearchService
    def elasticSearchAdminService
    def elasticSearchHelper
    private static final Logger LOG = Logger.getLogger(this);

    def setup() {
        // Make sure the indices are cleaned
        println "cleaning indices"
        elasticSearchAdminService.deleteIndex()
        elasticSearchAdminService.refresh()
    }

    def cleanupSpec() {
        def dataFolder = new File('data')
        if (dataFolder.isDirectory()) {
            dataFolder.deleteDir()
        }
    }

    def "Index a domain object"() {
        given:
        def product = new Product(name: "myTestProduct")
        product.save(failOnError: true)

        when:
        elasticSearchService.index(product)
        elasticSearchAdminService.refresh() // Ensure the latest operations have been exposed on the ES instance

        then:
        elasticSearchService.search("myTestProduct", [indices: Product, types: Product]).total == 1
    }

    def "Unindex method delete index from ES"() {
        given:
        def product = new Product(name: "myTestProduct")
        product.save(failOnError: true)

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
        product.save(failOnError: true)

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

    void "a geo point location is marshalled and demarshalled correctly"() {
        given:
        def location = new GeoPoint(
            lat: 53.00,
            lon: 10.00
        ).save(failOnError: true)

        def building = new Building(
            name: 'WatchTower',
            location: location
        ).save(failOnError: true)

        elasticSearchService.index(building)
        elasticSearchAdminService.refresh()

        when:
        def result = elasticSearchService.search('WatchTower', [indices: Building, types: Building])

        then:
        elasticSearchHelper.elasticSearchClient.admin().indices()

        result.total == 1
        result.searchResults[0].location == location
    }

    void "a geo point is mapped correctly"() {

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

        expect:
        def mapping = getFieldMappingMetaData("test", "building").sourceAsMap
        mapping.(properties).location.type == 'geo_point'
    }

    private MappingMetaData getFieldMappingMetaData(String indexName, String typeName) {
        AdminClient admin = elasticSearchHelper.elasticSearchClient.admin()
        ClusterAdminClient cluster = admin.cluster()

        ClusterStateRequestBuilder indices = cluster.prepareState().setFilterIndices(indexName)
        ClusterState clusterState = indices.execute().actionGet().state
        IndexMetaData indexMetaData = clusterState.metaData.index(indexName)
        return indexMetaData.mapping(typeName)
    }

    void "search with geo distance filter"() {
        given: "a building with a geo point location"
        GeoPoint geoPoint = new GeoPoint(
            lat: 53.12,
            lon: 10.12
        ).save(failOnError: true)
        def building = new Building(
            location: geoPoint
        ).save(failOnError: true)

        elasticSearchService.index(building)
        elasticSearchAdminService.refresh()

        when: "a geo distance filter search is performed"
        def searchResult = elasticSearchService.search([indices: Building, type: Building], null as Closure, {
            geo_distance: {
                distance: "50km"
                "building.location"(lat: 53.63, lon: 9.8)
            }
        })
        then: "the building should be found"
        1 == searchResult.searchResults.size()
        product1.name == searchResult.searchResults[0].name
    }

    void "searching with filtered query"() {
        given: "some products"
        def wurstProduct = new Product(name: "wurst", price: 2.00)
        wurstProduct.save(failOnError: true)

        def hansProduct = new Product(name: 'hans', price: 0.5)
        hansProduct.save(failOnError: true)

        def fooProduct = new Product(name: 'foo', price: 5.0)
        fooProduct.save(failOnError: true)

        elasticSearchService.index(wurstProduct, hansProduct, fooProduct)
        elasticSearchAdminService.refresh()

        when: "that a range filter find the product"
        def searchResult = elasticSearchService.search(null as Closure, {
            range {
                "price"(gte: 1, lte: 3)
            }
        })

        then: "the result should be product1"
        1 == searchResult.searchResults.size()
        wurstProduct.name == searchResult.searchResults[0].name


    }
}
