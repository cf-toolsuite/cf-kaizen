package org.cftoolsuite.cfapp.service.ai;

import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.cfapp.butler.api.ProductsApiClient;
import org.cftoolsuite.cfapp.butler.model.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductsService {

    private final ProductsApiClient productsApiClient;

    public ProductsService(ProductsApiClient productsApiClient) {
        this.productsApiClient = productsApiClient;
    }

    @Tool(name = "GetDeployedProducts", description = "(Butler) Get deployed products from Operations Manager.")
    public List<DeployedProduct> getDeployedProducts() {
        return productsApiClient.productsDeployedGet().getBody();
    }

    @Tool(name = "GetDeployedProductInsights", description =
        """
            (Butler) Assembles lists of deployed products from Operations Manager.
            Lists include: buildpacks, stemcells, and tiles.
            Each entry in each list is enhanced with release metadata from Tanzu Network.
        """)
    public ProductMetrics getProductMetrics() {
        return productsApiClient.productsMetricsGet().getBody();
    }

    @Tool(name = "GetOperationsManagerVersion", description = "(Butler) Get the version of Operations Manager.")
    public OmInfo getOmInfo() {
        return productsApiClient.productsOmInfoGet().getBody();
    }


    @Tool(name = "GetStemcellAssociations", description = "(Butler) Get stemcell associations from Operations Manager (v2.6+).")
    public StemcellAssociations getStemcellAssociations() {
        return productsApiClient.productsStemcellAssociationsGet().getBody();
    }

    @Tool(name = "GetProductInfoByName", description = "(Butler) Get product information by name from Tanzu Network.  Source: https://developer.broadcom.com/xapis/tanzu-api/latest/api/v2/products/get/.")
    public Product getProductByName(@ToolParam (description = "Product name pattern") String namePattern) {
        return Optional.ofNullable(productsApiClient.storeProductCatalogGet())
                .map(HttpEntity::getBody)
                .map(Products::getProducts)
                .flatMap(products -> products.stream()
                        .filter(product -> StringUtils.isNotBlank(product.getName()) &&
                                product.getName().toLowerCase().contains(namePattern.toLowerCase()))
                        .findFirst())
                .orElse(null);
    }

    @Tool(name = "GetLatestProductReleaseBySlug", description = "(Butler) Get latest product release by slug from Tanzu Network.  Source: https://developer.broadcom.com/xapis/tanzu-api/latest/api/v2/products/product_slug/releases/get/.")
    public Release getLatestProductReleaseBySlug(@ToolParam (description = "Product slug pattern") String slugPattern) {
        return Optional.ofNullable(productsApiClient.storeProductReleasesGet("latest"))
                .map(HttpEntity::getBody)
                .flatMap(releases -> releases.stream()
                        .filter(release -> StringUtils.isNotBlank(release.getSlug()) &&
                                release.getSlug().toLowerCase().contains(slugPattern.toLowerCase()))
                        .findFirst())
                .orElse(null);
    }

    @Tool(name = "GetRecentProductReleaseBySlug", description = "(Butler) Get recent product release by slug from Tanzu Network.  Source: https://developer.broadcom.com/xapis/tanzu-api/latest/api/v2/products/product_slug/releases/get/.")
    public Release getRecentProductReleaseBySlug(@ToolParam (description = "Product slug pattern") String slugPattern) {
        return Optional.ofNullable(productsApiClient.storeProductReleasesGet("recent"))
                .map(HttpEntity::getBody)
                .flatMap(releases -> releases.stream()
                        .filter(release -> StringUtils.isNotBlank(release.getSlug()) &&
                                release.getSlug().toLowerCase().contains(slugPattern.toLowerCase()))
                        .findFirst())
                .orElse(null);
    }

    @Tool(name = "GetAllProductReleasesBySlug", description = "(Butler) Get all product releases by slug from Tanzu Network.  Source: https://developer.broadcom.com/xapis/tanzu-api/latest/api/v2/products/product_slug/releases/get/.")
    public List<Release> getAllProductReleasesBySlug(@ToolParam(description = "Product slug pattern") String slugPattern) {
        return Optional.ofNullable(productsApiClient.storeProductReleasesGet("all"))
                .map(HttpEntity::getBody)
                .map(releases -> releases.stream()
                        .filter(release -> StringUtils.isNotBlank(release.getSlug()) &&
                                release.getSlug().toLowerCase().contains(slugPattern.toLowerCase()))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}