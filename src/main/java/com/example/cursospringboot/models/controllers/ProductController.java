package com.example.cursospringboot.models.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.example.cursospringboot.dtos.ProductRecordDto;
import com.example.cursospringboot.models.ProductModel;
import com.example.cursospringboot.models.repositories.ProductRepository;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;



@RestController
public class ProductController {
	
	@Autowired
	ProductRepository productRepository;
	
	//metodo para gravar
	@PostMapping("/products")
	public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto) {
		var productModel = new ProductModel();
		BeanUtils.copyProperties(productRecordDto, productModel);
		return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
	}
	
	//metodo para consultar todos os produtos
	@GetMapping("/products")
	public ResponseEntity<List<ProductModel>> getAllProducts() {
		
		List<ProductModel> productList = productRepository.findAll();
		
		if (!productList.isEmpty()) {
			for (ProductModel product : productList) {
				UUID id = product.getIdProduct();
				product.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(productList);
		
		//codigo abaixo funciona foi alterado apenas para teste usando o hateoas
		//return ResponseEntity.status(HttpStatus.OK).body(productRepository.findAll());
	}

	//metodo para consultar produtos por id
	@GetMapping("/products/{id}")
	public ResponseEntity<Object> getOneProduct(@PathVariable(value="id") UUID id) {
		Optional<ProductModel> productO = productRepository.findById(id);
		if (productO.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto não encontrado");
		}
		
		productO.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("Lista de Produtos"));
		return ResponseEntity.status(HttpStatus.OK).body(productO.get());
	}
	
	//metodo para atualizar produtos por id
	@PutMapping("products/{id}")
	public ResponseEntity<Object> updateProduct(@PathVariable(value="id") UUID id,
												@RequestBody @Valid ProductRecordDto productRecordDto) {
	    Optional<ProductModel> productO = productRepository.findById(id);
		
	    if (productO.isEmpty()) {
	    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto não encontrado.");
	    }
	    
	    var productModel = productO.get();
	    BeanUtils.copyProperties(productRecordDto, productModel);
	    
		return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
	}
	
	@DeleteMapping("products/{id}")
	public ResponseEntity<Object> deleteProduct(@PathVariable(value="id") UUID id) {
		Optional<ProductModel> produtoO = productRepository.findById(id);
		if(produtoO.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto não encontrado.");
		}
		productRepository.delete(produtoO.get());
		return ResponseEntity.status(HttpStatus.OK).body("Produto excluido com sucesso.");
	}
	
}
