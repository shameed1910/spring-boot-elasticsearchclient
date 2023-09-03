package com.example.springelasticjavaclient.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import com.example.springelasticjavaclient.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class ProductRepository {

    public static final String PRODUCTS = "products";
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public String createOrUpdateDocument( Product product ) throws IOException {
     IndexResponse  response= elasticsearchClient.index(i->i
             .index(PRODUCTS)
             .id(product.getId())
             .document(product));

     Map<String,String> responseMessages = Map.of(
             "Created","Document has been created",
             "Updated", "Document has been updated"
     );

     return responseMessages.getOrDefault(response.result().name(),"Error has occurred");

    }

    public Product findDocById(String productId) throws IOException {
        return elasticsearchClient.get(g->g.index(PRODUCTS).id(productId),Product.class).source();
    }

    public String deleteDocById(String productId) throws IOException {
        DeleteRequest deleteRequest = DeleteRequest.of(d->d.index(PRODUCTS).id(productId));
        DeleteResponse response =elasticsearchClient.delete(deleteRequest);

        return new StringBuffer(response.result().name().equalsIgnoreCase("NOT_FOUND")
                ?"Document not found with id"+productId:"Document has been deleted").toString();
    }

    public List<Product> findAll() throws IOException {
        SearchRequest request = SearchRequest.of(s->s.index(PRODUCTS));
       SearchResponse<Product> response = elasticsearchClient.search(request,Product.class);

       List<Product> products = new ArrayList<>();
       response.hits().hits().stream().forEach(object->{
           products.add(object.source());

       });
       return products;

    }

    public String bulkSave(List<Product> products) throws IOException {
        BulkRequest.Builder br = new BulkRequest.Builder();
        products.stream().forEach(product->br.operations(operation->
                operation.index(i->i
                        .index(PRODUCTS)
                        .id(product.getId())
                        .document(product))));

        BulkResponse response =elasticsearchClient.bulk(br.build());
        if(response.errors()){
            return new StringBuffer("Bulk has errors").toString();
        } else {
            return new StringBuffer("Bulk save success").toString();
        }
    }
}
