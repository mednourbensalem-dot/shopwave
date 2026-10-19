(() => {
  "use strict";

  const state = {
    products: [],
    categories: [],
    categoryId: null,
    query: "",
    sortBy: "id",
    direction: "asc",
    page: 0,
    size: 100,
    cart: null,
    user: null,
    auth: sessionStorage.getItem("shopwave.auth"),
    authMode: "login",
    toastTimer: null
  };
  const $ = (selector) => document.querySelector(selector);
  const grid = $("#product-grid");
  const money = (value) => new Intl.NumberFormat(undefined, { style: "currency", currency: "USD" }).format(Number(value || 0));
  const escapeHtml = (value) => String(value ?? "").replace(/[&<>"']/g, (char) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[char]);
  const basicHeader = () => state.auth ? { Authorization: `Basic ${state.auth}` } : {};

  async function request(url, options = {}) {
    const headers = { ...basicHeader(), ...(options.body ? { "Content-Type": "application/json" } : {}), ...(options.headers || {}) };
    const response = await fetch(url, { ...options, headers });
    if (response.status === 401) {
      state.auth = null;
      state.user = null;
      sessionStorage.removeItem("shopwave.auth");
      renderAccount();
      throw new Error("Please sign in to continue.");
    }
    if (!response.ok) {
      let message = `Request failed (${response.status}).`;
      try {
        const body = await response.json();
        message = body.message || body.error || message;
      } catch {
        // Keep the status-based message when the API response has no JSON body.
      }
      throw new Error(message);
    }
    if (response.status === 204) return null;
    return response.json();
  }

  function showToast(message) {
    const toast = $("#toast");
    toast.textContent = message;
    toast.classList.add("visible");
    clearTimeout(state.toastTimer);
    state.toastTimer = setTimeout(() => toast.classList.remove("visible"), 2600);
  }

  function setBusy(button, busy, text) {
    button.disabled = busy;
    if (busy) {
      button.dataset.previousText = button.innerHTML;
      button.textContent = text;
    } else if (button.dataset.previousText) {
      button.innerHTML = button.dataset.previousText;
      delete button.dataset.previousText;
    }
  }

  function productIcon(product) {
    const name = `${product.name || ""} ${product.category || ""}`.toLowerCase();
    if (/headphone|audio/.test(name)) return "🎧";
    if (/keyboard/.test(name)) return "⌨️";
    if (/usb|hub|electronic/.test(name)) return "🔌";
    if (/book|code|data/.test(name)) return "📚";
    if (/mug|coffee/.test(name)) return "☕";
    if (/lamp|light/.test(name)) return "💡";
    if (/pillow/.test(name)) return "🛏️";
    return "✳";
  }

  function renderCategories() {
    const pills = $("#category-pills");
    pills.innerHTML = `<button class="category-pill ${state.categoryId === null ? "active" : ""}" data-category="all" type="button">Everything</button>` +
      state.categories.map((category) => `<button class="category-pill ${state.categoryId === category.id ? "active" : ""}" data-category="${category.id}" type="button">${escapeHtml(category.name)}</button>`).join("");
    pills.querySelectorAll("[data-category]").forEach((button) => {
      button.addEventListener("click", () => {
        state.categoryId = button.dataset.category === "all" ? null : Number(button.dataset.category);
        renderCategories();
        loadProducts();
      });
    });
  }

  function renderProducts(products) {
    $("#result-count").textContent = `${products.length} ${products.length === 1 ? "good find" : "good finds"}`;
    $("#empty-state").classList.toggle("hidden", products.length > 0);
    grid.classList.toggle("hidden", products.length === 0);
    grid.innerHTML = products.map((product, index) => {
      const outOfStock = product.stock < 1;
      const image = product.imageUrl
        ? `<img class="product-image" src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.name)}" loading="lazy" onerror="this.remove()">`
        : `<span class="product-emoji" aria-hidden="true">${productIcon(product)}</span>`;
      return `<article class="product-card">
        <div class="product-art tone-${index % 5}">
          ${image}<span class="product-badge">${outOfStock ? "Out of stock" : escapeHtml(product.category || "A good find")}</span>
          <button class="quick-add" data-add="${product.id}" type="button" aria-label="Add ${escapeHtml(product.name)} to bag" ${outOfStock ? "disabled" : ""}>+</button>
        </div>
        <p class="product-category">${escapeHtml(product.category || "Shopwave pick")}</p>
        <h3 class="product-name">${escapeHtml(product.name)}</h3>
        <p class="product-description">${escapeHtml(product.description || "A thoughtful find for everyday life.")}</p>
        <div class="product-price-row"><span class="product-price">${money(product.price)}</span><span class="stock-note">${outOfStock ? "Currently unavailable" : `${product.stock} in stock`}</span></div>
      </article>`;
    }).join("");
    grid.querySelectorAll("[data-add]").forEach((button) => button.addEventListener("click", () => addToCart(Number(button.dataset.add))));
  }

  async function loadProducts() {
    $("#load-error").classList.add("hidden");
    grid.classList.remove("hidden");
    const params = new URLSearchParams({ page: String(state.page), size: String(state.size), sortBy: state.sortBy, direction: state.direction });
    if (state.query.trim()) params.set("q", state.query.trim());
    if (state.categoryId !== null) params.set("categoryId", String(state.categoryId));
    try {
      const page = await request(`/api/products?${params}`);
      renderProducts(page.content || []);
    } catch (error) {
      grid.innerHTML = "";
      grid.classList.add("hidden");
      $("#empty-state").classList.add("hidden");
      $("#load-error").classList.remove("hidden");
      $("#result-count").textContent = "Shop unavailable";
      if (!navigator.onLine) showToast("Check your connection and try again.");
      console.error("Unable to load product catalog:", error);
    }
  }

  function openPanel(element) {
    $("#overlay").classList.remove("hidden");
    element.classList.remove("hidden");
    if (element === $("#cart-drawer")) element.setAttribute("aria-hidden", "false");
  }

  function closePanels() {
    $("#overlay").classList.add("hidden");
    document.querySelectorAll(".drawer, .modal").forEach((element) => element.classList.add("hidden"));
    $("#cart-drawer").setAttribute("aria-hidden", "true");
  }

  function renderAccount() {
    const label = $("#account-label");
    const signed = $("#account-signed");
    if (state.user) {
      label.textContent = state.user.name.split(" ")[0];
      signed.innerHTML = `<p>Signed in as <strong>${escapeHtml(state.user.email)}</strong></p><div class="account-links"><button id="orders-button" type="button">View my orders</button><button id="logout-button" type="button">Sign out</button></div>`;
      $("#auth-form").classList.add("hidden");
      $("#demo-hint").classList.add("hidden");
      $(".auth-tabs").classList.add("hidden");
      signed.classList.remove("hidden");
      $("#account-title").innerHTML = `Hello, <em>${escapeHtml(state.user.name.split(" ")[0])}.</em>`;
      $("#orders-button").addEventListener("click", loadOrders);
      $("#logout-button").addEventListener("click", logout);
    } else {
      label.textContent = "Sign in";
      signed.classList.add("hidden");
      $("#auth-form").classList.remove("hidden");
      $("#demo-hint").classList.remove("hidden");
      $(".auth-tabs").classList.remove("hidden");
      $("#account-title").innerHTML = "Good to have <em>you.</em>";
    }
  }

  function updateCartCount() {
    const count = (state.cart?.items || []).reduce((sum, item) => sum + item.quantity, 0);
    $("#cart-count").textContent = count;
    $("#drawer-count").textContent = `(${count})`;
  }

  function renderCart() {
    const content = $("#cart-content");
    const items = state.cart?.items || [];
    updateCartCount();
    if (!state.user) {
      content.innerHTML = `<div class="empty-cart"><span class="empty-icon">♧</span><h3>Your bag is waiting.</h3><p>Sign in to add something good to your bag.</p><button class="button button-dark" id="cart-signin" type="button">Sign in <span>↗</span></button></div>`;
      $("#cart-signin").addEventListener("click", () => {
        closePanels();
        openPanel($("#account-modal"));
      });
      $("#cart-footer").classList.add("hidden");
      return;
    }
    if (!items.length) {
      content.innerHTML = `<div class="empty-cart"><span class="empty-icon">♧</span><h3>Your bag is taking a break.</h3><p>Go find something that makes your everyday a little better.</p></div>`;
      $("#cart-footer").classList.add("hidden");
      return;
    }
    content.innerHTML = items.map((item) => {
      const product = state.products.find((entry) => entry.id === item.productId);
      return `<div class="cart-line">
        <div class="cart-thumb">${product ? productIcon(product) : "✳"}</div>
        <div><h3>${escapeHtml(item.name)}</h3><p>${money(item.unitPrice)} each</p>
          <div class="quantity-control"><button data-quantity="${item.productId}" data-delta="-1" type="button" aria-label="Decrease quantity">−</button><span>${item.quantity}</span><button data-quantity="${item.productId}" data-delta="1" type="button" aria-label="Increase quantity">+</button></div>
        </div>
        <div class="cart-line-end"><strong>${money(item.subtotal)}</strong><button class="remove-item" data-remove="${item.productId}" type="button">Remove</button></div>
      </div>`;
    }).join("");
    content.querySelectorAll("[data-quantity]").forEach((button) => button.addEventListener("click", () => {
      const line = items.find((item) => item.productId === Number(button.dataset.quantity));
      changeQuantity(line.productId, line.quantity + Number(button.dataset.delta));
    }));
    content.querySelectorAll("[data-remove]").forEach((button) => button.addEventListener("click", () => removeFromCart(Number(button.dataset.remove))));
    $("#cart-total").textContent = money(state.cart.total);
    $("#cart-footer").classList.remove("hidden");
  }

  async function loadCart() {
    if (!state.auth || !state.user) {
      state.cart = null;
      renderCart();
      return;
    }
    try {
      state.cart = await request("/api/cart");
      renderCart();
    } catch (error) {
      state.cart = null;
      renderCart();
      showToast(error.message);
    }
  }

  async function addToCart(productId) {
    if (!state.user) {
      openPanel($("#account-modal"));
      showToast("Sign in to add good finds to your bag.");
      return;
    }
    try {
      state.cart = await request("/api/cart/items", { method: "POST", body: JSON.stringify({ productId, quantity: 1 }) });
      renderCart();
      showToast("Added to your bag ✳");
    } catch (error) {
      showToast(error.message);
    }
  }

  async function changeQuantity(productId, quantity) {
    try {
      state.cart = quantity < 1
        ? await request(`/api/cart/items/${productId}`, { method: "DELETE" })
        : await request("/api/cart/items", { method: "PUT", body: JSON.stringify({ productId, quantity }) });
      renderCart();
    } catch (error) {
      showToast(error.message);
    }
  }

  async function removeFromCart(productId) {
    try {
      state.cart = await request(`/api/cart/items/${productId}`, { method: "DELETE" });
      renderCart();
      showToast("Removed from your bag.");
    } catch (error) {
      showToast(error.message);
    }
  }

  async function loadOrders() {
    closePanels();
    openPanel($("#orders-modal"));
    $("#orders-content").innerHTML = `<p class="orders-empty">Gathering your orders…</p>`;
    try {
      const orders = await request("/api/orders");
      if (!orders.length) {
        $("#orders-content").innerHTML = `<p class="orders-empty">No orders yet. Your next good find is waiting in the shop.</p>`;
        return;
      }
      $("#orders-content").innerHTML = orders.map((order) => {
        const date = new Date(order.createdAt).toLocaleDateString();
        const summary = order.items.map((item) => `${item.quantity} × ${escapeHtml(item.productName)}`).join(", ");
        return `<article class="order-card"><div class="order-card-top"><span>Order #${order.id} · ${date}</span><span class="order-status">${escapeHtml(order.status)}</span></div><p>${summary}</p><p>${money(order.total)} · Ship to ${escapeHtml(order.shippingAddress)}</p></article>`;
      }).join("");
    } catch (error) {
      $("#orders-content").innerHTML = `<p class="orders-empty">${escapeHtml(error.message)}</p>`;
    }
  }

  function logout() {
    sessionStorage.removeItem("shopwave.auth");
    state.auth = null;
    state.user = null;
    state.cart = null;
    renderAccount();
    renderCart();
    closePanels();
    showToast("You are signed out.");
  }

  function setAuthMode(mode) {
    state.authMode = mode;
    const register = mode === "register";
    document.querySelectorAll("[data-auth-mode]").forEach((button) => button.classList.toggle("active", button.dataset.authMode === mode));
    $("#name-field").classList.toggle("hidden", !register);
    $("#name-field input").required = register;
    $("#auth-form [name=password]").autocomplete = register ? "new-password" : "current-password";
    $("#auth-submit").innerHTML = `${register ? "Create account" : "Sign in"} <span>↗</span>`;
    $("#auth-message").textContent = "";
    $("#auth-message").classList.remove("success");
  }

  async function submitAuth(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    const button = $("#auth-submit");
    const message = $("#auth-message");
    message.textContent = "";
    setBusy(button, true, state.authMode === "register" ? "Creating account…" : "Signing in…");
    try {
      const email = String(data.get("email")).trim().toLowerCase();
      const password = String(data.get("password"));
      if (state.authMode === "register") {
        await request("/api/auth/register", { method: "POST", body: JSON.stringify({ name: String(data.get("name")).trim(), email, password }) });
      }
      state.auth = btoa(`${email}:${password}`);
      sessionStorage.setItem("shopwave.auth", state.auth);
      state.user = await request("/api/auth/me");
      renderAccount();
      await loadCart();
      closePanels();
      form.reset();
      showToast(state.authMode === "register" ? "Welcome to ShopWave!" : `Welcome back, ${state.user.name.split(" ")[0]}!`);
    } catch (error) {
      state.auth = null;
      state.user = null;
      sessionStorage.removeItem("shopwave.auth");
      message.textContent = error.message;
    } finally {
      setBusy(button, false);
    }
  }

  async function submitCheckout(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const button = $("#place-order");
    const message = $("#checkout-message");
    message.textContent = "";
    setBusy(button, true, "Placing your order…");
    try {
      const result = await request("/api/orders/checkout", {
        method: "POST",
        body: JSON.stringify({ shippingAddress: String(new FormData(form).get("shippingAddress")).trim() })
      });
      state.cart = { items: [], total: 0 };
      renderCart();
      form.reset();
      closePanels();
      await loadProducts();
      showToast(`Order #${result.id} is confirmed. Thank you!`);
    } catch (error) {
      message.textContent = error.message;
    } finally {
      setBusy(button, false);
    }
  }

  async function initialize() {
    $("#year").textContent = String(new Date().getFullYear());
    renderAccount();
    renderCart();
    $("#load-error").classList.add("hidden");
    try {
      state.categories = await request("/api/categories");
      renderCategories();
      if (state.auth) {
        try {
          state.user = await request("/api/auth/me");
          renderAccount();
          await loadCart();
        } catch (error) {
          console.warn("Saved ShopWave sign-in is no longer valid:", error);
        }
      }
      await loadProducts();
    } catch (error) {
      console.error("Unable to initialize ShopWave:", error);
      $("#load-error").classList.remove("hidden");
    }
  }

  let searchTimer;
  $("#search-input").addEventListener("input", (event) => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => {
      state.query = event.target.value;
      state.page = 0;
      loadProducts();
    }, 250);
  });
  $("#sort-select").addEventListener("change", (event) => {
    [state.sortBy, state.direction] = event.target.value.split(",");
    loadProducts();
  });
  document.querySelectorAll("[data-category-link]").forEach((link) => link.addEventListener("click", () => {
    const category = state.categories.find((item) => item.name.toLowerCase() === link.dataset.categoryLink.toLowerCase());
    if (category) {
      state.categoryId = category.id;
      renderCategories();
      loadProducts();
    }
  }));
  $("#reset-filters").addEventListener("click", () => {
    state.query = "";
    state.categoryId = null;
    state.page = 0;
    $("#search-input").value = "";
    renderCategories();
    loadProducts();
  });
  $("#retry-load").addEventListener("click", initialize);
  $("#account-button").addEventListener("click", () => {
    openPanel($("#account-modal"));
    $("#auth-message").textContent = "";
  });
  $("#cart-button").addEventListener("click", async () => {
    openPanel($("#cart-drawer"));
    await loadCart();
  });
  $("#checkout-button").addEventListener("click", () => {
    if (!state.user) {
      closePanels();
      openPanel($("#account-modal"));
      return;
    }
    if (!(state.cart?.items || []).length) return;
    closePanels();
    openPanel($("#checkout-modal"));
    $("#checkout-message").textContent = "";
  });
  document.querySelectorAll("[data-auth-mode]").forEach((button) => button.addEventListener("click", () => setAuthMode(button.dataset.authMode)));
  document.querySelectorAll("[data-close]").forEach((button) => button.addEventListener("click", closePanels));
  $("#overlay").addEventListener("click", closePanels);
  $("#auth-form").addEventListener("submit", submitAuth);
  $("#checkout-form").addEventListener("submit", submitCheckout);
  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") closePanels();
  });
  initialize();
})();
