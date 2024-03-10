package com.mohamedalizehri.mystore.services;

import com.mohamedalizehri.mystore.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepository extends JpaRepository<Product, Integer> {
}
