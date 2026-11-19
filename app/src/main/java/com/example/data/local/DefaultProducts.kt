package com.example.data.local

import com.example.data.model.Product

object DefaultProducts {
    fun getList(): List<Product> = listOf(
        Product(
            id = "p1",
            name = "أرز الضحى أو الساعة فاخر",
            category = "grocery",
            price = 29.0,
            originalPrice = 34.0,
            isFlashDeal = true,
            dealBadge = "وفر 5 ج.م ⚡",
            weight = "1 كجم",
            imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=500"
        ),
        Product(
            id = "p2",
            name = "سكر أبيض ناعم مميز",
            category = "grocery",
            price = 29.5,
            originalPrice = 35.0,
            isFlashDeal = true,
            dealBadge = "سوبر توفير 🔥",
            weight = "1 كجم",
            imageUrl = "https://images.unsplash.com/photo-1581441363689-1ed3dcf2d4d1?w=500"
        ),
        Product(
            id = "p3",
            name = "زيت عافية ذرة / عباد الشمس",
            category = "grocery",
            price = 72.0,
            originalPrice = 85.0,
            isFlashDeal = true,
            dealBadge = "خصم 15% ⚡",
            weight = "800 مل",
            imageUrl = "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=500"
        ),
        Product(
            id = "p4",
            name = "سمن روابي أو الهانم أصلي",
            category = "grocery",
            price = 95.0,
            weight = "700 جرام",
            imageUrl = "https://images.unsplash.com/photo-1589985270860-0e7820980c57?w=500"
        ),
        Product(
            id = "p5",
            name = "مكرونة الملكة أو الإيطالية",
            category = "grocery",
            price = 22.0,
            weight = "400 جرام",
            imageUrl = "https://images.unsplash.com/photo-1621996346565-e3d5d6281298?w=500"
        ),
        Product(
            id = "p6",
            name = "صلصة طماطم هارفست / جهينه",
            category = "grocery",
            price = 16.0,
            weight = "300 جرام",
            imageUrl = "https://images.unsplash.com/photo-1541592106381-b31e9677c0e5?w=500"
        ),
        Product(
            id = "p7",
            name = "شاي العروسة أو ليبتون الناعم",
            category = "beverages",
            price = 55.0,
            weight = "250 جرام",
            imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=500"
        ),
        Product(
            id = "p8",
            name = "حليب جهينه أو المراعي طبيعي",
            category = "dairy",
            price = 42.0,
            weight = "1 لتر",
            imageUrl = "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=500"
        ),
        Product(
            id = "p9",
            name = "جبنة بريزيدون مثلثات أو فلاحي",
            category = "dairy",
            price = 48.0,
            weight = "عبوة 8 قطع",
            imageUrl = "https://images.unsplash.com/photo-1559561853-08451507cbe7?w=500"
        ),
        Product(
            id = "p10",
            name = "بيض طازج أحمر مميز",
            category = "dairy",
            price = 145.0,
            originalPrice = 165.0,
            isFlashDeal = true,
            dealBadge = "وفر 20 ج.م 🥚",
            weight = "كرتونة 30 بيضة",
            imageUrl = "https://images.unsplash.com/photo-1516448835821-39a4891152a5?w=500"
        ),
        Product(
            id = "p11",
            name = "تفاح أمريكي أحمر طازج",
            category = "fruits",
            price = 60.0,
            weight = "1 كجم",
            imageUrl = "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=500"
        ),
        Product(
            id = "p12",
            name = "موز بلدي سكري طازج",
            category = "fruits",
            price = 30.0,
            weight = "1 كجم",
            imageUrl = "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=500"
        ),
        Product(
            id = "p13",
            name = "طماطم بلدي حمراء طازجة",
            category = "fruits",
            price = 18.0,
            weight = "1 كجم",
            imageUrl = "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=500"
        ),
        Product(
            id = "p14",
            name = "بطاطس تحمير وقلي طازجة",
            category = "fruits",
            price = 20.0,
            weight = "1 كجم",
            imageUrl = "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=500"
        ),
        Product(
            id = "p15",
            name = "خبز بلدي طازج مخابز",
            category = "bakery",
            price = 10.0,
            weight = "5 أرغفة",
            imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500"
        ),
        Product(
            id = "p16",
            name = "مسحوق غسيل إريال أوتوماتيك",
            category = "cleaning",
            price = 105.0,
            originalPrice = 125.0,
            isFlashDeal = true,
            dealBadge = "وفر 20 ج.م 🧹",
            weight = "2.5 كجم",
            imageUrl = "https://images.unsplash.com/photo-1585842378054-ee2e52f94ba2?w=500"
        )
    )
}
