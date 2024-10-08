/**
 * Author: Lê Nguyên Minh Quý 27/06/1998
 */
package fashion.mock.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fashion.mock.model.Discount;
import fashion.mock.model.Image;
import fashion.mock.model.Product;
import fashion.mock.repository.ImageRepository;
import fashion.mock.repository.ProductRepository;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private FileStorageService fileStorageService;

	public Product addProduct(Product product, List<MultipartFile> imageFiles) {
		validateProduct(product);
		product.setCreatedDate(LocalDate.now());
		Product savedProduct = productRepository.save(product);

		if (imageFiles != null && !imageFiles.isEmpty()) {
			for (MultipartFile file : imageFiles) {
				String imagePath = fileStorageService.storeFile(file);
				Image image = new Image();
				image.setImgLink(imagePath);
				image.setProduct(savedProduct);
				imageRepository.save(image);
			}
		}

		return savedProduct;
	}

	public Product updateProduct(Product product, List<MultipartFile> imageFiles, List<Long> deletedImageIds) {
		validateProduct(product);
		Product existingProduct = productRepository.findById(product.getId())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + product.getId()));

		existingProduct.setProductName(product.getProductName().trim());
		existingProduct.setDescription(product.getDescription());
		existingProduct.setPrice(product.getPrice());
		existingProduct.setQuantity(product.getQuantity());
		existingProduct.setColor(product.getColor());
		existingProduct.setSize(product.getSize());
		existingProduct.setCategory(product.getCategory());
		existingProduct.setUpdatedDate(LocalDate.now());

		// Handle photo deletion
		if (deletedImageIds != null && !deletedImageIds.isEmpty()) {
			for (Long imageId : deletedImageIds) {
				Image imageToDelete = imageRepository.findById(imageId).orElse(null);
				if (imageToDelete != null && imageToDelete.getProduct().getId().equals(existingProduct.getId())) {
					existingProduct.getImages().remove(imageToDelete);
					imageRepository.delete(imageToDelete);
					// Delete image files if necessary
					fileStorageService.deleteFile(imageToDelete.getImgLink());
				}
			}
		}

		Product updatedProduct = productRepository.save(existingProduct);

		// Process new images
		if (imageFiles != null && !imageFiles.isEmpty()) {
			for (MultipartFile file : imageFiles) {
				if (!file.isEmpty()) {
					String imagePath = fileStorageService.storeFile(file);
					Image image = new Image();
					image.setImgLink(imagePath);
					image.setProduct(updatedProduct);
					imageRepository.save(image);
					updatedProduct.getImages().add(image);
				}
			}
		}

		return productRepository.save(updatedProduct);
	}

	private void validateProduct(Product product) {
		if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
			throw new IllegalArgumentException("Tên sản phẩm không được để trống");
		}
		String trimmedName = product.getProductName().trim();
		Product existingProduct = productRepository.findByProductName(trimmedName);
		if (existingProduct != null && !existingProduct.getId().equals(product.getId())) {
			throw new IllegalArgumentException("Tên sản phẩm đã tồn tại");
		}
		product.setProductName(trimmedName);
	}

	public boolean deleteProduct(Long id) {
		if (productRepository.existsById(id)) {
			Product product = productRepository.findById(id).get();
			for (Image image : product.getImages()) {
				fileStorageService.deleteFile(image.getImgLink());
			}
			productRepository.deleteById(id);
			return true;
		} else {
			return false;
		}
	}

	public Page<Product> getAllProducts(Pageable pageable) {
		return productRepository.findAll(pageable);
	}

	public Optional<Product> getProductById(Long id) {
		return productRepository.findById(id);
	}

	/**
	 * @author Tran Thien Thanh 09/04/1996
	 */
	public Product updateProduct(Product product) {
		return productRepository.save(product);
	}

	public Page<Product> searchProducts(String searchTerm, Pageable pageable) {
		if (searchTerm == null || searchTerm.trim().isEmpty()) {
			return productRepository.findAll(pageable);
		}
		return productRepository.searchByName(searchTerm.trim(), pageable);
	}

	public boolean deleteImage(Long imageId) {
		Optional<Image> imageOpt = imageRepository.findById(imageId);
		if (imageOpt.isPresent()) {
			Image image = imageOpt.get();
			fileStorageService.deleteFile(image.getImgLink());
			imageRepository.delete(image);
			return true;
		}
		return false;
	}

	public Page<Product> getAllProducts(PageRequest pageRequest) {
		return productRepository.findAll(pageRequest);
	}

	public Page<Product> searchProducts(String searchTerm, PageRequest pageRequest) {
		return productRepository.findByProductNameContainingIgnoreCase(searchTerm, pageRequest);
	}

	public Page<Product> getAllProductsSortedByPriceAsc(PageRequest pageRequest) {
		return productRepository.findAllByOrderByPriceAsc(pageRequest);
	}

	public Page<Product> getAllProductsSortedByPriceDesc(PageRequest pageRequest) {
		return productRepository.findAllByOrderByPriceDesc(pageRequest);
	}

	public Page<Product> searchProductsSortedByPriceAsc(String searchTerm, PageRequest pageRequest) {
		return productRepository.findByProductNameContainingIgnoreCaseOrderByPriceAsc(searchTerm, pageRequest);
	}

	public Page<Product> searchProductsSortedByPriceDesc(String searchTerm, PageRequest pageRequest) {
		return productRepository.findByProductNameContainingIgnoreCaseOrderByPriceDesc(searchTerm, pageRequest);
	}

	private static final Map<String, List<String>> COLOR_MAPPING = new HashMap<>();
	static {
		COLOR_MAPPING.put("White", Arrays.asList("White", "Trắng"));
		COLOR_MAPPING.put("Yellow", Arrays.asList("Yellow", "Vàng"));
		COLOR_MAPPING.put("Red", Arrays.asList("Red", "Đỏ"));
		COLOR_MAPPING.put("Green", Arrays.asList("Green", "Xanh lá"));
		COLOR_MAPPING.put("Blue", Arrays.asList("Blue", "Xanh dương"));
		COLOR_MAPPING.put("Purple", Arrays.asList("Purple", "Tím"));
		COLOR_MAPPING.put("Orange", Arrays.asList("Orange", "Cam"));
		COLOR_MAPPING.put("Pink", Arrays.asList("Pink", "Hồng"));
		COLOR_MAPPING.put("Black", Arrays.asList("Black", "Đen"));
	}

	public Page<Product> getFilteredProducts(String searchTerm, String sortBy, String color, String size,
			String priceRange, Long categoryId, PageRequest pageRequest) {
		Specification<Product> spec = Specification.where(null);

		if (searchTerm != null && !searchTerm.isEmpty()) {
			spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("productName")),
					"%" + searchTerm.toLowerCase() + "%"));
		}

		if (color != null && !color.isEmpty()) {
			spec = spec.and((root, query, cb) -> {
				List<String> colorVariants = getColorVariants(color);
				return root.get("color").in(colorVariants);
			});
		}

		if (size != null && !size.isEmpty()) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("size"), size));
		}
		if (priceRange != null && !priceRange.isEmpty()) {
			String[] range = priceRange.split("-");
			Double minPrice = parsePrice(range[0]); // The minimum value is always there

			Double maxPrice;
			if (range.length > 1 && !range[1].isEmpty()) {
				// If there is a maximum value then convert
				maxPrice = parsePrice(range[1]);
			} else {
				// If there is no maximum value, set it to Double.MAX_VALUE
				maxPrice = Double.MAX_VALUE;
			}

			// Apply price filter
			spec = spec.and((root, query, cb) -> cb.between(root.get("price"), minPrice, maxPrice));
		}

		if (categoryId != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
		}

		if ("priceAsc".equals(sortBy)) {
			return productRepository.findAll(spec, pageRequest.withSort(Sort.by(Sort.Direction.ASC, "price")));
		} else if ("priceDesc".equals(sortBy)) {
			return productRepository.findAll(spec, pageRequest.withSort(Sort.by(Sort.Direction.DESC, "price")));
		} else {
			return productRepository.findAll(spec, pageRequest);
		}
	}

	private Double parsePrice(String priceStr) {
		if (priceStr == null || priceStr.isEmpty()) {
			return 0.0; // Default value if string is empty
		}
		String cleanedStr = priceStr.replaceAll("[^0-9.]", "").replace(".", "");
		return Double.parseDouble(cleanedStr);
	}

	private List<String> getColorVariants(String color) {
		for (Map.Entry<String, List<String>> entry : COLOR_MAPPING.entrySet()) {
			if (entry.getValue().stream().anyMatch(c -> c.equalsIgnoreCase(color))) {
				return entry.getValue();
			}
		}
		return Arrays.asList(color);
	}

	public boolean isProductOnDiscount(Product product) {
		LocalDate now = LocalDate.now();
		return product.getDiscounts().stream()
				.anyMatch(discount -> !discount.getStartDate().isAfter(now) && !discount.getEndDate().isBefore(now));
	}

	public double getDiscountedPrice(Product product) {
		if (isProductOnDiscount(product)) {
			Discount activeDiscount = product.getDiscounts().stream()
					.filter(discount -> !discount.getStartDate().isAfter(LocalDate.now())
							&& !discount.getEndDate().isBefore(LocalDate.now()))
					.findFirst().orElse(null);

			if (activeDiscount != null) {
				return product.getPrice() * (1 - activeDiscount.getDiscountPercent() / 100);
			}
		}
		return product.getPrice();
	}

	public List<Product> getProductsByCategory(String categoryName) {
		return productRepository.findByCategory_CategoryName(categoryName);
	}

	public List<Product> getTop4NewProducts() {
		return productRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate")).stream().limit(4)
				.collect(Collectors.toList());
	}

	//huan

	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	public Product findProductById(Long id) {
		return productRepository.findById(id).orElse(null);
	}
}
