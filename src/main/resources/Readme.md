Some improvements were intentionally left out due to time constraints, but they would be valuable additions to enhance the project’s robustness and usability:

1-Unit Tests & Integration Tests

While the service works correctly, the test coverage could be significantly expanded.

    -More unit tests would improve validation of business logic in isolation.
    -Additional integration tests would help ensure that the API, cache layer, and external API client work smoothly together under real conditions.

2-Docker Support for Easy Setup 

Creating a simple Docker setup (Dockerfile + docker-compose) would allow interviewers and other developers to run the project quickly without installing dependencies manually.
This was left out to focus on the core implementation first, but it would be a valuable improvement for developer experience.

3-More Advanced Caching Strategy

The current cache works well for the challenge, but production usage might benefit from:

    -Distributed caching (Redis)
    -Cache invalidation policies
    -Background cache refresh
    -Cache observability metrics

These improvements were left out to keep the solution simple and aligned with challenge expectations.