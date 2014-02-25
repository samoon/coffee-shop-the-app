package com.mechanitis.demo.coffee

import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.MongoClient
import org.bson.types.ObjectId
import spock.lang.Specification

import javax.ws.rs.core.Response

class CoffeeShopResourceSpecification extends Specification {
    def 'should return a dummy shop for testing'() {
        given:
        def coffeeShop = new CoffeeShopResource(null)

        when:
        def nearestShop = coffeeShop.getDummy()

        then:
        nearestShop.name == 'A dummy coffee shop'
    }

    def 'should return Cafe Nero as the closest coffee shop to Westminster Abbey'() {
        given:
        def mongoClient = new MongoClient()
        def coffeeShop = new CoffeeShopResource(mongoClient.getDB("TrishaCoffee"))

        when:
        double latitude = 51.4994678
        double longitude = -0.128888
        def nearestShop = coffeeShop.getNearest(latitude, longitude)

        then:
        nearestShop.name == 'Caffè Nero'
        println nearestShop.allValues
    }

    def 'should return Costa as the closest coffee shop to Earls Court Road'() {
        given:
        def mongoClient = new MongoClient()
        def coffeeShop = new CoffeeShopResource(mongoClient.getDB("TrishaCoffee"))

        when:
        double latitude = 51.4950233
        double longitude = -0.1962431
        def nearestShop = coffeeShop.getNearest(latitude, longitude)

        then:
        nearestShop.name == 'Costa'
        println nearestShop.allValues
    }

    def 'should return null if no coffee shop found'() {
        given:
        def mongoClient = new MongoClient()
        def coffeeShop = new CoffeeShopResource(mongoClient.getDB("TrishaCoffee"))

        when:
        double latitude = 37.3981841
        double longitude = -5.9776375999999996
        def nearestShop = coffeeShop.getNearest(latitude, longitude)

        then:
        nearestShop == null
    }

    def 'should give me back the order ID when an order is successfully created'() {
        given:
        DB database = Mock()
        database.getCollection(_) >> { Mock(DBCollection) }

        def coffeeShop = new CoffeeShopResource(database)
        def order = new Order(new String[0], new DrinkType('espresso', 'coffee'), 'medium', 'Me')

        //set ID for testing
        def orderId = new ObjectId()
        order.setId(orderId)

        when:
        Response response = coffeeShop.saveOrder(75847854, order);

        then:
        response != null
        response.status == Response.Status.CREATED.statusCode
        response.headers['Location'][0].toString() == orderId.toString()
    }

    //functional test
    def 'should save all fields to the database when order is saved'() {
        given:
        def mongoClient = new MongoClient()
        def database = mongoClient.getDB("TrishaCoffee")
        def collection = database.getCollection('orders')
        collection.drop();

        def coffeeShop = new CoffeeShopResource(database)

        String[] orderOptions = ['soy milk']
        def drinkType = new DrinkType('espresso', 'coffee')
        def size = 'medium'
        def coffeeDrinker = 'Me'
        def order = new Order(orderOptions, drinkType, size, coffeeDrinker)

        when:
        Response response = coffeeShop.saveOrder(89438, order);

        then:
        collection.count == 1
        def createdOrder = collection.findOne()
        createdOrder['selectedOptions'] == orderOptions
        createdOrder['type'].name == drinkType.name
        createdOrder['type'].family == drinkType.family
        createdOrder['size'] == size
        createdOrder['drinker'] == coffeeDrinker
        createdOrder['_id'] != null
        println createdOrder
        //    form = {
        //        "selectedOptions": [],
        //        "type": {
        //            "name": "Cappuccino",
        //            "family": "Coffee"
        //        },
        //        "size": "Small",
        //        "drinker": "Trisha"
        //    }
    }

}
