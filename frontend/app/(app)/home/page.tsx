"use client";

import Link from "next/link";
import { useState } from "react";
import { ShoppingCart, Star, User } from "lucide-react";

function Navbar({ query, setQuery }: { query: string; setQuery: (q: string) => void }) {
  const [cartCount, setCartCount] = useState(3); // Example initial count

  return (
    <nav className="bg-white shadow-lg fixed top-0 left-0 w-full z-10 border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-6 py-4 flex justify-between items-center">
        {/* Logo */}
        <Link href="/" className="flex items-center space-x-2">
          <img src="/images/logo.png" alt="Online Store Logo" className="h-10 w-auto" />
          <span className="text-xl font-bold text-gray-800 hidden md:block">Online Store</span>
        </Link>

        {/* Search bar */}
        <div className="hidden md:flex items-center border border-gray-300 rounded-lg overflow-hidden w-1/2">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search products..."
            className="px-4 py-2 w-full focus:outline-none"
          />
        </div>

        {/* Icons */}
        <div className="flex gap-6 items-center">
          {/* Favorites */}
          <Star size={24} />
          {/* <Link href="/favorites" className="text-gray-700 hover:text-[#7621FF] transition-colors">
          </Link> */}

          {/* Checkout with badge */}
          <div className="relative">
            <ShoppingCart size={24} />
            {/* <Link href="/checkout" className="text-gray-700 hover:text-[#7621FF] transition-colors">
              {cartCount > 0 && (
                <span className="absolute -top-2 -right-2 bg-red-600 text-white text-xs w-5 h-5 flex items-center justify-center rounded-full">
                  {cartCount}
                </span>
              )}
            </Link> */}
          </div>

          {/* Profile */}
          <User size={24} />
          {/* <Link href="/profile" className="text-gray-700 hover:text-[#7621FF] transition-colors">
          </Link> */}
        </div>
      </div>
    </nav>
  );
}

export default function HomePage() {
  const placeholderProducts = [
    { id: 1, name: "Laptop", price: "$999", category: "Electronics", image: "/images/laptop.jpeg" },
    { id: 2, name: "Headphones", price: "$199", category: "Electronics", image: "/images/headphones.jpg" },
    { id: 3, name: "T-Shirt", price: "$29", category: "Clothing", image: "/images/tshirt.jpeg" },
    { id: 4, name: "Coffee Maker", price: "$49", category: "Home", image: "/images/coffee.jpeg" },
    { id: 5, name: "Basketball", price: "$39", category: "Sports", image: "/images/basketball.jpeg" },
    { id: 6, name: "Book: Learn JAVA", price: "$19", category: "Books", image: "/images/java.jpg" },
    { id: 7, name: "Book: The Hunger Games", price: "$10", category: "Books", image: "/images/hungergames.jpeg" },
    { id: 8, name: "Book: Books of Doom", price: "$20", category: "Books", image: "/images/doom.jpeg" },
    { id: 9, name: "Dress", price: "$25", category: "Clothing", image: "/images/dress.jpg" },
    { id: 10, name: "Volleyball", price: "$30", category: "Sports", image: "/images/vball.jpeg" },
    { id: 11, name: "Workout Bag", price: "$25", category: "Sports", image: "/images/bag.jpeg" },
    { id: 12, name: "Fan", price: "$40", category: "Sports", image: "/images/fan.jpeg" },
  ];

  const [query, setQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("All");

  const categories = ["All", ...Array.from(new Set(placeholderProducts.map((p) => p.category)))];

  // Filter products by category AND search query
  const filteredProducts = placeholderProducts.filter(
    (product) =>
      (selectedCategory === "All" || product.category === selectedCategory) &&
      product.name.toLowerCase().includes(query.toLowerCase())
  );

  return (
    <>
      <Navbar query={query} setQuery={setQuery} />

      <div className="max-w-6xl mx-auto p-6 pt-36">
        <h1 className="text-3xl font-bold mb-6">Welcome to Online Store!</h1>
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
          {filteredProducts.map((product) => (
            <div key={product.id} className="bg-white border rounded-lg p-4 shadow hover:shadow-lg transition">
              <div className="h-40 mb-4 rounded overflow-hidden flex items-center justify-center">
                <img
                  src={product.image}
                  alt={product.name}
                  className="object-cover w-full h-full"
                />
              </div>
              <h2 className="text-lg font-semibold">{product.name}</h2>
              <p className="text-gray-700">{product.price}</p>
              <p className="text-sm text-gray-500">{product.category}</p>
              <button className="mt-3 w-full bg-[#7621FF] text-white px-4 py-2 rounded hover:bg-purple-700">
                Add to Cart
              </button>
            </div>
          ))}
          {filteredProducts.length === 0 && (
            <p className="text-gray-500 col-span-full">No products found.</p>
          )}
        </div>
      </div>
    </>
  );
}
