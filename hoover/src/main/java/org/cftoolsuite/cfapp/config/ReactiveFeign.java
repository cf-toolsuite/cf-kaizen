package org.cftoolsuite.cfapp.config;

import feign.Contract;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class ReactiveFeign {

    @Bean
    public Decoder feignDecoder() {
        return new ResponseEntityDecoder(new SpringDecoder(() ->
                new HttpMessageConverters(new MappingJackson2HttpMessageConverter())));
    }

    @Bean
    public Encoder feignEncoder() {
        return new SpringEncoder(() ->
                new HttpMessageConverters(new MappingJackson2HttpMessageConverter()));
    }

    @Bean
    public Contract feignContract() {
        return new SpringMvcContract();
    }

}