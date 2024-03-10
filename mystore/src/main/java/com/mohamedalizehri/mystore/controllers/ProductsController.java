package com.mohamedalizehri.mystore.controllers;

import com.mohamedalizehri.mystore.models.Product;
import com.mohamedalizehri.mystore.models.ProductDto;
import com.mohamedalizehri.mystore.services.ProductsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductsRepository repo;

    @GetMapping({"","/"})
    public String showProductList (Model model){
        List <Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return"products/index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model){
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result
    ){
        if(productDto.getImageFile().isEmpty()){
            result.addError(
                    new FieldError(
                    "productDto",
                    "imageFile",
                    "The image File is required"));
        }

        if(result.hasErrors()){
            return "products/CreateProduct";
        }
        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);

        repo.save(product);
        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id) {
        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            productDto.setDescription(product.getDescription());

            model.addAttribute("ProductDto", productDto);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return "redirect:/products";
        }
        return "products/EditProduct";
    }
    @PostMapping("/edit")
    public String updateproduct(Model model,
                                @RequestParam int id,
                                @Valid @ModelAttribute ProductDto productDto,
                                BindingResult result) {

        try {
            Product product = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product id: " + id));
            model.addAttribute("product", product);

            if (result.hasErrors()) {
                return "products/EditProduct";
            }

            if (!productDto.getImageFile().isEmpty()) {

                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());
                try {
                    Files.delete(oldImagePath);
                } catch (IOException ex) {
                    System.out.println("Exception while deleting old image: " + ex.getMessage());
                }

                MultipartFile image = productDto.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    // Handle exceptions related to file copying
                    System.out.println("Exception while saving new image: " + ex.getMessage());
                }

                product.setImageFileName(storageFileName);
            }
            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());

            repo.save(product);

        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        return "redirect:/products";
    }
    @GetMapping("/delete")
    public String deleteProduct(
            @RequestParam int id
    ) {
        Product product = null;
        try {
            product = repo.findById(id).get();
            Path imagePath = Paths.get("public/images/" + product.getImageFileName());
        } catch (Exception e) {
            System.out.println("Exception :" + e.getMessage());
        }

        repo.delete(product);
        return "redirect:/products";
    }

}
