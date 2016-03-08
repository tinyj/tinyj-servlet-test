# TinyJ ServletTest

Mocks for HttpServletRequest and HttpServletResponse to facilitate testing of
Servlets.


## PREVIEW

This code is currently in preview state. There should be more tests and a
documentation. `HttpServletResponseMock` is in a usable state,
`HttpServletRequestMock` is lacking proper session/authentication support,
multipart support and support for parameters from request body. Both classes
are lacking async support.

There will be no servlet context support.


## API documentation

You can find the API documentation [here](./APIdoc.md).


## License

If not stated otherwise all files are released under the under the Apache
License, Version 2.0 (the "License"); you may not use any file except in
compliance with the License. You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
