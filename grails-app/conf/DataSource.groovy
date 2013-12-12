// environment specific settings
environments {
    development {
        hibernate {
            cache.use_second_level_cache = true
            cache.use_query_cache = true
            cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
        }

        dataSource {
            pooled = true
            driverClassName = "org.h2.Driver"
            username = "sa"
            password = ""

            dbCreate = "update"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE"
        }
    }
    test {
        hibernate {
            cache.use_second_level_cache = true
            cache.use_query_cache = true
            cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
        }

        dataSource {
            pooled = true
            driverClassName = "org.h2.Driver"
            username = "sa"
            password = ""

            dbCreate = "update"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE"
        }
    }
}
