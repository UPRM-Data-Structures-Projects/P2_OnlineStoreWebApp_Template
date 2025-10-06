"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { ShoppingCart, Star, User } from "lucide-react";
import axios, { AxiosError } from "axios";

// ===== axios client =====
const api = axios.create({
  baseURL: "http://localhost:9090",
  headers: { "Content-Type": "application/json" },
  // withCredentials: true, // uncomment if your API uses cookies
});

// ===== helpers =====
const priceToNumber = (s: string) => {
  const n = parseFloat(String(s).replace(/[^0-9.]/g, ""));
  return isNaN(n) ? 0 : n;
};
const numberToMoney = (n: number) => `$${(+n).toFixed(2)}`;
const getErr = (e: unknown) => {
  const ax = e as AxiosError<any>;
  return (
    (ax.response?.data && (ax.response.data.message || ax.response.data.error)) ||
    ax.message ||
    "Request failed."
  );
};

// --- Navbar Component Definition ---
function Navbar({ query, setQuery }: { query: string; setQuery: (q: string) => void }) {
  const [cartCount] = useState(3);

  return (
    <nav className="bg-[#fcfbfa] shadow-lg fixed top-0 left-0 w-full z-10 border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-6 py-4 flex justify-between items-center">
        <Link href="/" className="flex items-center space-x-2">
          <img src="/images/logo2.png" alt="Online Store Logo" className="h-20 w-auto" />
          <span className="text-xl font-bold text-gray-800 hidden md:block"></span>
        </Link>

        <div className="hidden md:flex items-center border border-gray-300 rounded-lg overflow-hidden w-1/2">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search products..."
            className="px-4 py-2 w-full focus:outline-none"
          />
        </div>

        <div className="flex gap-6 items-center">
          <Link href="/favorites" className="text-gray-700 hover:text-[#7621FF] transition-colors">
            <Star size={24} />
          </Link>
          <div className="relative">
            <ShoppingCart size={24} />
          </div>
          <Link href="/profile" className="text-gray-700 hover:text-[#7621FF] transition-colors">
            <User size={24} />
          </Link>
        </div>
      </div>
    </nav>
  );
}

// --- HomePage Component Definition (The main export) ---
export default function HomePage() {
  // Keeping your placeholder list (unused once API loads, but left intact as requested)
  const placeholderProducts = [
    { id: 1, name: "Template", price: "$999", category: "Template", image: "", description: "Template" },
  ];

  // === State (same as yours) ===
  const [products, setProducts] = useState(placeholderProducts); // will be replaced by API data
  const [query, setQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [selectedProduct, setSelectedProduct] = useState<any | null>(null);
  const [editProduct, setEditProduct] = useState<typeof placeholderProducts[0] | null>(null);
  const [newProduct, setNewProduct] = useState<typeof placeholderProducts[0] | null>(null);

  // Extras: loading/error (non-intrusive)
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // === Load from backend ===
  useEffect(() => {
    (async () => {
      setLoading(true);
      setError("");
      try {
        // Expecting: [{ id, name, price:number, category, currency?, image }]
        const { data } = await api.get("/api/products");
        // Map price:number -> your UI price:string with "$"
        const mapped = (data || []).map((p: any) => ({
          ...p,
          price: typeof p.price === "number" ? numberToMoney(p.price) : p.price,
          // keep image coming from backend as you asked
        }));
        setProducts(mapped);
      } catch (e) {
        setError(getErr(e));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  // === Derived ===
  const categories = useMemo(
    () => ["All", ...Array.from(new Set(products.map((p) => p.category)))],
    [products]
  );

  const filteredProducts = useMemo(
    () =>
      products.filter(
        (product) =>
          (selectedCategory === "All" || product.category === selectedCategory) &&
          product.name.toLowerCase().includes(query.toLowerCase())
      ),
    [products, selectedCategory, query]
  );

  // === Your existing handlers (kept) ===
  const handleEditChange = (field: keyof typeof placeholderProducts[0], value: string) => {
    if (!editProduct) return;
    if (field === "price" && !value.startsWith("$")) {
      value = "$" + value.replace("$", "");
    }
    setEditProduct({ ...editProduct, [field]: value });
  };

  const handleNewChange = (field: keyof typeof placeholderProducts[0], value: string) => {
    if (!newProduct) return;
    if (field === "price" && !value.startsWith("$")) {
      value = "$" + value.replace("$", "");
    }
    setNewProduct({ ...newProduct, [field]: value });
  };

  // === New: CRUD calls (used in buttons below) ===
  const refresh = async () => {
    try {
      const { data } = await api.get("/api/products");
      const mapped = (data || []).map((p: any) => ({
        ...p,
        price: typeof p.price === "number" ? numberToMoney(p.price) : p.price,
      }));
      setProducts(mapped);
    } catch (e) {
      setError(getErr(e));
    }
  };

  const apiAdd = async (p: any) => {
    await api.post("/api/products", {
      id: p.id,
      name: p.name,
      price: priceToNumber(p.price),
      category: p.category,
      currency: p.currency || "USD",
      image: p.image, // backend can store/return it
      description: p.description, // if backend supports it
    });
  };

  const apiUpdate = async (p: any) => {
    await api.put(`/api/products/${p.id}`, {
      name: p.name,
      price: priceToNumber(p.price),
      category: p.category,
      currency: p.currency || "USD",
      image: p.image,
      description: p.description,
    });
  };

  const apiDelete = async (id: number) => {
    await api.delete(`/api/products/${id}`);
  };

  return (
    <>
      {/* Navbar */}
      <Navbar query={query} setQuery={setQuery} />

      <div className="max-w-6xl mx-auto p-6 pt-36">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold">Welcome to Online Store!</h1>
          <button
            onClick={() =>
              setNewProduct({
                id: products.length ? Math.max(...products.map((p) => p.id)) + 1 : 1,
                name: "",
                price: "$0",
                category: "",
                image: "",
                description: "",
              })
            }
            className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600 transition-colors"
          >
            Add Product
          </button>
        </div>

        {error && (
          <div className="mb-4 rounded border border-red-300 bg-red-50 text-red-700 px-4 py-2">
            {error}
          </div>
        )}

        <p className="text-gray-600 mb-8">Browse our collection of amazing products.</p>

        {/* Category filter */}
        <div className="flex gap-4 mb-6 overflow-x-auto">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-4 py-2 rounded border ${selectedCategory === cat
                ? "bg-[#7621FF] text-white"
                : "bg-white text-gray-700 hover:bg-gray-100"
                }`}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Products Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {loading && (
            <p className="text-gray-500 col-span-full text-center py-10">Loading…</p>
          )}
          {!loading &&
            filteredProducts.map((product) => (
              <div key={product.id} className="bg-white border rounded-lg p-4 shadow hover:shadow-lg transition">
                <div className="h-40 mb-4 rounded overflow-hidden flex items-center justify-center bg-gray-50">
                  {product.image.length > 0 &&
                    <img src={product.image} alt={product.name} className="object-contain w-full h-full" />
                  }
                </div>
                <h2 className="text-lg font-semibold">{product.name}</h2>
                <p className="text-gray-700">{product.price}</p>
                <p className="text-sm text-gray-500">{product.category}</p>
                <div className="flex flex-col gap-2 mt-3">
                  <button
                    className="w-full bg-[#7621FF] text-white px-4 py-2 rounded hover:bg-purple-700 transition-colors"
                    onClick={() => setSelectedProduct(product)}
                  >
                    More Info
                  </button>
                </div>
              </div>
            ))}
          {!loading && filteredProducts.length === 0 && (
            <p className="text-gray-500 col-span-full text-center py-10">
              No products found matching your criteria.
            </p>
          )}
        </div>
      </div>

      {/* View Product Modal */}
      {selectedProduct && (
        <div className="fixed inset-0 flex items-center justify-center z-50 bg-gray-200 bg-opacity-50 p-4">
          <div className="bg-white rounded-lg shadow-2xl max-w-md w-full p-6 relative transform transition-all scale-100">
            <button
              onClick={() => setSelectedProduct(null)}
              className="absolute top-3 right-3 text-gray-500 hover:text-gray-800 transition-colors"
              aria-label="Close"
            >
              ✕
            </button>
            <div className="mb-4 border-b pb-4">
              <img src={selectedProduct.image} alt={selectedProduct.name} className="w-full max-h-60 object-contain rounded" />
            </div>
            <h2 className="text-2xl font-bold mb-2">{selectedProduct.name}</h2>
            <p className="text-green-600 text-xl font-semibold mb-1">{selectedProduct.price}</p>
            <p className="text-sm text-gray-500 mb-4 border-b pb-4">Category: {selectedProduct.category}</p>
            <p className="text-gray-600">{selectedProduct.description}</p>
            <div className="mt-6 flex gap-4">
              <button
                className="flex-1 bg-yellow-500 text-white py-2 rounded hover:bg-yellow-600 transition-colors"
                onClick={() => {
                  setEditProduct(selectedProduct);
                  setSelectedProduct(null);
                }}
              >
                Edit Product
              </button>
              <button
                onClick={async () => {
                  try {
                    await apiDelete(selectedProduct.id);
                    await refresh();
                  } catch (e) {
                    setError(getErr(e));
                  } finally {
                    setSelectedProduct(null);
                  }
                }}
                className="flex-1 bg-red-500 text-white py-2 rounded hover:bg-red-600 transition-colors"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Product Modal */}
      {editProduct && (
        <div className="fixed inset-0 flex items-center justify-center z-50 bg-gray-200 bg-opacity-50 p-4">
          <div className="bg-white rounded-lg shadow-2xl max-w-md w-full p-6 relative transform transition-all scale-100">
            <button
              onClick={() => setEditProduct(null)}
              className="absolute top-3 right-3 text-gray-500 hover:text-gray-800 transition-colors"
              aria-label="Close"
            >
              ✕
            </button>
            <h2 className="text-xl font-bold mb-4 border-b pb-2">Edit Product: {editProduct.name}</h2>
            <div className="flex flex-col gap-3">
              <input type="text" value={editProduct.name} onChange={(e) => handleEditChange("name", e.target.value)} placeholder="Product Name" className="border px-3 py-2 rounded focus:ring-2 focus:ring-[#7621FF] focus:border-transparent transition" />
              <input type="text" value={editProduct.price} onChange={(e) => handleEditChange("price", e.target.value)} placeholder="Price (e.g., $9.99)" className="border px-3 py-2 rounded focus:ring-2 focus:ring-[#7621FF] focus:border-transparent transition" />
              <input type="text" value={editProduct.category} onChange={(e) => handleEditChange("category", e.target.value)} placeholder="Category" className="border px-3 py-2 rounded focus:ring-2 focus:ring-[#7621FF] focus:border-transparent transition" />
              <textarea value={editProduct.description || ""} onChange={(e) => handleEditChange("description", e.target.value)} placeholder="Description" className="border px-3 py-2 rounded resize-none focus:ring-2 focus:ring-[#7621FF] focus:border-transparent transition" rows={3} />
            </div>
            <div className="mt-6 flex justify-end gap-2">
              <button
                onClick={async () => {
                  try {
                    await apiUpdate(editProduct);
                    await refresh();
                  } catch (e) {
                    setError(getErr(e));
                  } finally {
                    setEditProduct(null);
                  }
                }}
                className="bg-[#7621FF] text-white px-4 py-2 rounded hover:bg-purple-700 transition-colors font-semibold"
              >
                Save Changes
              </button>
              <button onClick={() => setEditProduct(null)} className="bg-gray-200 text-gray-800 px-4 py-2 rounded hover:bg-gray-300 transition-colors">Cancel</button>
            </div>
          </div>
        </div>
      )}

      {/* Add Product Modal */}
      {newProduct && (
        <div className="fixed inset-0 flex items-center justify-center z-50 bg-gray-200 bg-opacity-50 p-4">
          <div className="bg-white rounded-lg shadow-2xl max-w-md w-full p-6 relative transform transition-all scale-100">
            <button onClick={() => setNewProduct(null)} className="absolute top-3 right-3 text-gray-500 hover:text-gray-800 transition-colors" aria-label="Close">✕</button>
            <h2 className="text-xl font-bold mb-4 border-b pb-2">Add New Product</h2>
            <div className="flex flex-col gap-3">
              <input type="text" value={newProduct.name} onChange={(e) => handleNewChange("name", e.target.value)} placeholder="Product Name" className="border px-3 py-2 rounded focus:ring-2 focus:ring-[#7621FF] focus:border-transparent transition" />
              <input type="text" value={newProduct.price} onChange={(e) => handleNewChange("price", e.target.value)} placeholder="Price (e.g., $9.99)" className="border px-3 py-2 rounded focus:ring-2 focus:ring-[#7621FF] focus:border-transparent transition" />
              <input type="text" value={newProduct.category} onChange={(e) => handleNewChange("category", e.target.value)} placeholder="Category" className="border px-3 py-2 rounded focus:ring-2 focus:ring-[#7621FF] focus:border-transparent transition" />
              <input type="text" value={newProduct.image} onChange={(e) => handleNewChange("image", e.target.value)} placeholder="Image URL" className="border px-3 py-2 rounded focus:ring-2 focus:ring-[#7621FF] focus:border-transparent transition" />
              <textarea value={newProduct.description} onChange={(e) => handleNewChange("description", e.target.value)} placeholder="Description" className="border px-3 py-2 rounded resize-none focus:ring-2 focus:ring-[#7621FF] focus:border-transparent transition" rows={3} />
            </div>
            <div className="mt-6 flex justify-end gap-2">
              <button
                onClick={async () => {
                  try {
                    await apiAdd(newProduct);
                    await refresh();
                  } catch (e) {
                    setError(getErr(e));
                  } finally {
                    setNewProduct(null);
                  }
                }}
                className="bg-[#7621FF] text-white px-4 py-2 rounded hover:bg-purple-700 transition-colors font-semibold"
              >
                Add Product
              </button>
              <button onClick={() => setNewProduct(null)} className="bg-gray-200 text-gray-800 px-4 py-2 rounded hover:bg-gray-300 transition-colors">Cancel</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
