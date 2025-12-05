/*
 * App Bootstrap Core
 * Copyright (C) 2025
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.bootstrap.core.ddd;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SpecificationTest {
    // Test domain object
    private static class Product {
        private final String name;
        private final double price;
        private final boolean inStock;

        public Product(String name, double price, boolean inStock) {
            this.name = name;
            this.price = price;
            this.inStock = inStock;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public boolean isInStock() {
            return inStock;
        }
    }

    // Concrete specifications for testing
    private static class PriceAboveSpecification implements ISpecification<Product> {
        private final double threshold;

        public PriceAboveSpecification(double threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean isSatisfiedBy(Product candidate) {
            return candidate.getPrice() > threshold;
        }
    }

    private static class InStockSpecification implements ISpecification<Product> {
        @Override
        public boolean isSatisfiedBy(Product candidate) {
            return candidate.isInStock();
        }
    }

    private static class NameContainsSpecification implements ISpecification<Product> {
        private final String text;

        public NameContainsSpecification(String text) {
            this.text = text;
        }

        @Override
        public boolean isSatisfiedBy(Product candidate) {
            return candidate.getName().toLowerCase().contains(text.toLowerCase());
        }
    }

    @Nested
    @DisplayName("Basic Specification Tests")
    class BasicSpecificationTests {

        @Test
        @DisplayName("Should satisfy when price is above threshold")
        void shouldSatisfyWhenPriceIsAboveThreshold() {
            // Arrange
            Product product = new Product("Laptop", 1000.0, true);
            ISpecification<Product> spec = new PriceAboveSpecification(500.0);

            // Act & Assert
            assertTrue(spec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should not satisfy when price is below threshold")
        void shouldNotSatisfyWhenPriceIsBelowThreshold() {
            // Arrange
            Product product = new Product("Mouse", 25.0, true);
            ISpecification<Product> spec = new PriceAboveSpecification(50.0);

            // Act & Assert
            assertFalse(spec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should satisfy when product is in stock")
        void shouldSatisfyWhenProductIsInStock() {
            // Arrange
            Product product = new Product("Keyboard", 75.0, true);
            ISpecification<Product> spec = new InStockSpecification();

            // Act & Assert
            assertTrue(spec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should not satisfy when product is not in stock")
        void shouldNotSatisfyWhenProductIsNotInStock() {
            // Arrange
            Product product = new Product("Monitor", 300.0, false);
            ISpecification<Product> spec = new InStockSpecification();

            // Act & Assert
            assertFalse(spec.isSatisfiedBy(product));
        }
    }

    @Nested
    @DisplayName("AND Specification Tests")
    class AndSpecificationTests {

        @Test
        @DisplayName("Should satisfy when both specifications are satisfied")
        void shouldSatisfyWhenBothSpecificationsAreSatisfied() {
            // Arrange
            Product product = new Product("Laptop", 1000.0, true);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(500.0);
            ISpecification<Product> stockSpec = new InStockSpecification();
            ISpecification<Product> combinedSpec = priceSpec.and(stockSpec);

            // Act & Assert
            assertTrue(combinedSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should not satisfy when first specification is not satisfied")
        void shouldNotSatisfyWhenFirstSpecificationIsNotSatisfied() {
            // Arrange
            Product product = new Product("Mouse", 25.0, true);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(500.0);
            ISpecification<Product> stockSpec = new InStockSpecification();
            ISpecification<Product> combinedSpec = priceSpec.and(stockSpec);

            // Act & Assert
            assertFalse(combinedSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should not satisfy when second specification is not satisfied")
        void shouldNotSatisfyWhenSecondSpecificationIsNotSatisfied() {
            // Arrange
            Product product = new Product("Laptop", 1000.0, false);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(500.0);
            ISpecification<Product> stockSpec = new InStockSpecification();
            ISpecification<Product> combinedSpec = priceSpec.and(stockSpec);

            // Act & Assert
            assertFalse(combinedSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should not satisfy when both specifications are not satisfied")
        void shouldNotSatisfyWhenBothSpecificationsAreNotSatisfied() {
            // Arrange
            Product product = new Product("Mouse", 25.0, false);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(500.0);
            ISpecification<Product> stockSpec = new InStockSpecification();
            ISpecification<Product> combinedSpec = priceSpec.and(stockSpec);

            // Act & Assert
            assertFalse(combinedSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should chain multiple AND specifications")
        void shouldChainMultipleAndSpecifications() {
            // Arrange
            Product product = new Product("Gaming Laptop", 1500.0, true);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(1000.0);
            ISpecification<Product> stockSpec = new InStockSpecification();
            ISpecification<Product> nameSpec = new NameContainsSpecification("Gaming");
            ISpecification<Product> combinedSpec = priceSpec.and(stockSpec).and(nameSpec);

            // Act & Assert
            assertTrue(combinedSpec.isSatisfiedBy(product));
        }
    }

    @Nested
    @DisplayName("OR Specification Tests")
    class OrSpecificationTests {

        @Test
        @DisplayName("Should satisfy when both specifications are satisfied")
        void shouldSatisfyWhenBothSpecificationsAreSatisfied() {
            // Arrange
            Product product = new Product("Laptop", 1000.0, true);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(500.0);
            ISpecification<Product> stockSpec = new InStockSpecification();
            ISpecification<Product> combinedSpec = priceSpec.or(stockSpec);

            // Act & Assert
            assertTrue(combinedSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should satisfy when only first specification is satisfied")
        void shouldSatisfyWhenOnlyFirstSpecificationIsSatisfied() {
            // Arrange
            Product product = new Product("Laptop", 1000.0, false);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(500.0);
            ISpecification<Product> stockSpec = new InStockSpecification();
            ISpecification<Product> combinedSpec = priceSpec.or(stockSpec);

            // Act & Assert
            assertTrue(combinedSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should satisfy when only second specification is satisfied")
        void shouldSatisfyWhenOnlySecondSpecificationIsSatisfied() {
            // Arrange
            Product product = new Product("Mouse", 25.0, true);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(500.0);
            ISpecification<Product> stockSpec = new InStockSpecification();
            ISpecification<Product> combinedSpec = priceSpec.or(stockSpec);

            // Act & Assert
            assertTrue(combinedSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should not satisfy when both specifications are not satisfied")
        void shouldNotSatisfyWhenBothSpecificationsAreNotSatisfied() {
            // Arrange
            Product product = new Product("Mouse", 25.0, false);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(500.0);
            ISpecification<Product> stockSpec = new InStockSpecification();
            ISpecification<Product> combinedSpec = priceSpec.or(stockSpec);

            // Act & Assert
            assertFalse(combinedSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should chain multiple OR specifications")
        void shouldChainMultipleOrSpecifications() {
            // Arrange
            Product product = new Product("Mouse", 25.0, false);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(500.0);
            ISpecification<Product> stockSpec = new InStockSpecification();
            ISpecification<Product> nameSpec = new NameContainsSpecification("Mouse");
            ISpecification<Product> combinedSpec = priceSpec.or(stockSpec).or(nameSpec);

            // Act & Assert
            assertTrue(combinedSpec.isSatisfiedBy(product));
        }
    }

    @Nested
    @DisplayName("NOT Specification Tests")
    class NotSpecificationTests {

        @Test
        @DisplayName("Should satisfy when original specification is not satisfied")
        void shouldSatisfyWhenOriginalSpecificationIsNotSatisfied() {
            // Arrange
            Product product = new Product("Mouse", 25.0, true);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(500.0);
            ISpecification<Product> notSpec = priceSpec.not();

            // Act & Assert
            assertTrue(notSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should not satisfy when original specification is satisfied")
        void shouldNotSatisfyWhenOriginalSpecificationIsSatisfied() {
            // Arrange
            Product product = new Product("Laptop", 1000.0, true);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(500.0);
            ISpecification<Product> notSpec = priceSpec.not();

            // Act & Assert
            assertFalse(notSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should double negate back to original")
        void shouldDoubleNegateBackToOriginal() {
            // Arrange
            Product product = new Product("Laptop", 1000.0, true);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(500.0);
            ISpecification<Product> doubleNotSpec = priceSpec.not().not();

            // Act & Assert
            assertTrue(doubleNotSpec.isSatisfiedBy(product));
        }
    }

    @Nested
    @DisplayName("Complex Specification Combinations")
    class ComplexSpecificationTests {

        @Test
        @DisplayName("Should handle (A AND B) OR C pattern")
        void shouldHandleAndOrPattern() {
            // Arrange
            Product product = new Product("Budget Mouse", 15.0, true);
            ISpecification<Product> expensiveSpec = new PriceAboveSpecification(500.0);
            ISpecification<Product> inStockSpec = new InStockSpecification();
            ISpecification<Product> nameSpec = new NameContainsSpecification("Mouse");

            // (expensive AND inStock) OR containsMouse
            ISpecification<Product> complexSpec = expensiveSpec.and(inStockSpec).or(nameSpec);

            // Act & Assert
            assertTrue(complexSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should handle A AND (B OR C) pattern")
        void shouldHandleAndWithOrPattern() {
            // Arrange
            Product product = new Product("Gaming Laptop", 1500.0, false);
            ISpecification<Product> expensiveSpec = new PriceAboveSpecification(1000.0);
            ISpecification<Product> inStockSpec = new InStockSpecification();
            ISpecification<Product> nameSpec = new NameContainsSpecification("Gaming");

            // expensive AND (inStock OR containsGaming)
            ISpecification<Product> complexSpec = expensiveSpec.and(inStockSpec.or(nameSpec));

            // Act & Assert
            assertTrue(complexSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should handle NOT (A OR B) pattern")
        void shouldHandleNotOrPattern() {
            // Arrange
            Product product = new Product("Cable", 5.0, false);
            ISpecification<Product> expensiveSpec = new PriceAboveSpecification(100.0);
            ISpecification<Product> inStockSpec = new InStockSpecification();

            // NOT (expensive OR inStock)
            ISpecification<Product> complexSpec = expensiveSpec.or(inStockSpec).not();

            // Act & Assert
            assertTrue(complexSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should handle NOT A AND NOT B pattern (De Morgan's law)")
        void shouldHandleDeMorganPattern() {
            // Arrange
            Product product = new Product("Cable", 5.0, false);
            ISpecification<Product> expensiveSpec = new PriceAboveSpecification(100.0);
            ISpecification<Product> inStockSpec = new InStockSpecification();

            // NOT expensive AND NOT inStock
            ISpecification<Product> complexSpec = expensiveSpec.not().and(inStockSpec.not());

            // Act & Assert
            assertTrue(complexSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should handle complex nested specifications")
        void shouldHandleComplexNestedSpecifications() {
            // Arrange
            Product product = new Product("Premium Gaming Laptop", 2000.0, true);
            ISpecification<Product> veryExpensiveSpec = new PriceAboveSpecification(1500.0);
            ISpecification<Product> expensiveSpec = new PriceAboveSpecification(1000.0);
            ISpecification<Product> inStockSpec = new InStockSpecification();
            ISpecification<Product> gamingSpec = new NameContainsSpecification("Gaming");

            // (veryExpensive OR (expensive AND gaming)) AND inStock
            ISpecification<Product> complexSpec =
                    veryExpensiveSpec.or(expensiveSpec.and(gamingSpec)).and(inStockSpec);

            // Act & Assert
            assertTrue(complexSpec.isSatisfiedBy(product));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Null Handling")
    class EdgeCaseTests {

        @Test
        @DisplayName(
                "Should handle specification with null candidate gracefully in custom implementation")
        void shouldHandleNullCandidate() {
            // Arrange
            ISpecification<Product> nullSafeSpec =
                    new ISpecification<Product>() {
                        @Override
                        public boolean isSatisfiedBy(Product candidate) {
                            return candidate != null && candidate.isInStock();
                        }
                    };

            // Act & Assert
            assertFalse(nullSafeSpec.isSatisfiedBy(null));
        }

        @Test
        @DisplayName("Should handle empty string in name specification")
        void shouldHandleEmptyString() {
            // Arrange
            Product product = new Product("", 100.0, true);
            ISpecification<Product> nameSpec = new NameContainsSpecification("Laptop");

            // Act & Assert
            assertFalse(nameSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should handle zero price threshold")
        void shouldHandleZeroPriceThreshold() {
            // Arrange
            Product product = new Product("Free Item", 0.0, true);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(0.0);

            // Act & Assert
            assertFalse(priceSpec.isSatisfiedBy(product));
        }

        @Test
        @DisplayName("Should handle negative price")
        void shouldHandleNegativePrice() {
            // Arrange
            Product product = new Product("Refund", -50.0, true);
            ISpecification<Product> priceSpec = new PriceAboveSpecification(0.0);

            // Act & Assert
            assertFalse(priceSpec.isSatisfiedBy(product));
        }
    }
}
