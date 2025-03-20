package org.cftoolsuite.cfapp.service.ai;

import org.cftoolsuite.cfapp.butler.api.ProductsApiClient;
import org.cftoolsuite.cfapp.butler.model.DeployedProduct;
import org.cftoolsuite.cfapp.butler.model.OmInfo;
import org.cftoolsuite.cfapp.butler.model.ProductMetrics;
import org.cftoolsuite.cfapp.butler.model.Products;
import org.cftoolsuite.cfapp.butler.model.Release;
import org.cftoolsuite.cfapp.butler.model.StemcellAssignments;
import org.cftoolsuite.cfapp.butler.model.StemcellAssociations;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductsService {

    private final ProductsApiClient productsApiClient;

    public ProductsService(ProductsApiClient productsApiClient) {
        this.productsApiClient = productsApiClient;
    }

    @Tool(description = "Get deployed products from Ops Manager.")
    public List<DeployedProduct> getDeployedProducts() {
        return productsApiClient.productsDeployedGet().getBody();
    }

    @Tool(description = "Get product metrics from Ops Manager and Pivnet.")
    public ProductMetrics getProductMetrics() {
        return productsApiClient.productsMetricsGet().getBody();
    }

    @Tool(description = "Get Ops Manager info.")
    public OmInfo getOmInfo() {
        return productsApiClient.productsOmInfoGet().getBody();
    }

    @Tool(description = "Get stemcell assignments from Ops Manager.")
    public StemcellAssignments getStemcellAssignments() {
        return productsApiClient.productsStemcellAssignmentsGet().getBody();
    }

    @Tool(description = "Get stemcell associations from Ops Manager (v2.6+).")
    public StemcellAssociations getStemcellAssociations() {
        return productsApiClient.productsStemcellAssociationsGet().getBody();
    }

    @Tool(description = "Get product catalog from Pivnet.")
    public Products getProductCatalog() {
        return productsApiClient.storeProductCatalogGet().getBody();
    }

    @Tool(description = "Get product releases from Pivnet.")
    public List<Release> getProductReleases(@ToolParam(description = "Query option (latest, all, recent).") String q) {
        return productsApiClient.storeProductReleasesGet(q).getBody();
    }
}