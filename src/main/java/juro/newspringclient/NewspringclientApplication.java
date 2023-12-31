package juro.newspringclient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class NewspringclientApplication {
	public static void main(String[] args) {
		SpringApplication.run(NewspringclientApplication.class, args);
	}

	@Bean
	ApplicationRunner applicationRunner(CustomerHttpClient cc) {
		return args -> {
			cc.all().subscribe(System.out::println);
			cc.byName("Luis").subscribe(System.out::println);
		};
	}

	@Bean
	CustomerHttpClient client(WebClient.Builder builder) {
		var wc = builder.baseUrl("http://localhost:8080").build();
		var wca = WebClientAdapter.forClient(wc);

		return HttpServiceProxyFactory.builder()
			.clientAdapter(wca)
			.build()
			.createClient(CustomerHttpClient.class);
	}
}

interface CustomerHttpClient {

	@GetExchange("/customers")
	Flux<Customer> all();

	@GetExchange("/customers/{name}")
	Flux<Customer> byName(@PathVariable String name);
}

record Customer(Long id, String name) {
}

record Profile(Long id) {
}

@Controller
class CustomerGraphqlController {
	private final CustomerHttpClient cc;

	public CustomerGraphqlController(CustomerHttpClient cc) {
		this.cc = cc;
	}

	@QueryMapping
	Flux<Customer> customers() {
		return this.cc.all();
	}

	@BatchMapping
	Map<Customer, Profile> profile(List<Customer> customers) {
		var map = new HashMap<Customer, Profile>();
		for (var c : customers)
			map.put(c, new Profile(c.id()));
		return map;
	}

}
