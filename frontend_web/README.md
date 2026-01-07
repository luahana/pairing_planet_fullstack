# Pairing Planet - Web (Next.js)

Web frontend for the Pairing Planet recipe evolution platform, optimized for SEO and discoverability.

## Status: Planned (Long-Term)

This project is planned but not yet implemented. See [ROADMAP.md](../docs/ai/ROADMAP.md#web-version-for-seo--discoverability) for timeline.

## Purpose

- Enable recipe discovery via search engines (Google, Naver)
- Support social sharing with proper Open Graph meta tags
- Provide full feature parity with mobile app on desktop

## Tech Stack

- **Framework**: Next.js 14+ (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Auth**: NextAuth.js (Google, Naver, Kakao, Apple)
- **Deployment**: AWS (ECS Fargate) or GCP (Cloud Run)

## Planned Project Structure

```
pairing_planet_web/
├── app/
│   ├── page.tsx                    # Home (trending recipes)
│   ├── recipes/
│   │   ├── page.tsx                # Browse all recipes
│   │   ├── new/page.tsx            # Create recipe (protected)
│   │   └── [id]/
│   │       ├── page.tsx            # Recipe detail (SSG/ISR)
│   │       ├── edit/page.tsx       # Edit recipe (protected)
│   │       ├── variation/page.tsx  # Create variation (protected)
│   │       └── log/page.tsx        # Create cooking log (protected)
│   ├── u/[username]/page.tsx       # User profile (SSR)
│   ├── profile/page.tsx            # My profile (protected)
│   ├── saved/page.tsx              # Saved recipes (protected)
│   ├── auth/
│   │   ├── login/page.tsx          # Login page
│   │   └── callback/page.tsx       # OAuth callback
│   ├── api/auth/[...nextauth]/route.ts  # NextAuth API
│   ├── sitemap.ts                  # Dynamic sitemap
│   └── layout.tsx                  # Root layout
├── components/
│   ├── RecipeCard.tsx
│   ├── RecipeDetail.tsx
│   ├── RecipeForm.tsx
│   ├── LogForm.tsx
│   ├── ImageUploader.tsx
│   └── AuthGuard.tsx
├── lib/
│   ├── api.ts                      # Backend API client
│   ├── auth.ts                     # Auth utilities
│   └── seo.ts                      # Meta tag helpers
├── contexts/
│   └── AuthContext.tsx
└── public/
    └── robots.txt
```

## SEO Features

- **SSR/SSG**: Pre-rendered HTML for search engine crawlers
- **ISR**: Incremental Static Regeneration (5 min revalidation for recipes)
- **Schema.org**: Recipe structured data for Google Rich Results
- **Open Graph**: Dynamic meta tags for social sharing previews
- **Sitemap**: Auto-generated sitemap.xml from recipe data

## Backend Requirements

Before implementing this web frontend, the backend needs:

1. **CORS Configuration** - Allow web origins
2. **SEO Controller** - `GET /api/v1/seo/recipes/{id}` for meta tags
3. **Sitemap Controller** - `GET /api/v1/recipes/public-ids` for sitemap generation

## Quick Start (Future)

```bash
# Install dependencies
npm install

# Set up environment variables
cp .env.example .env.local
# Edit .env.local with your values

# Run development server
npm run dev

# Build for production
npm run build

# Start production server
npm start
```

## Environment Variables (Future)

```env
# Backend API
NEXT_PUBLIC_API_URL=http://localhost:4001/api/v1

# NextAuth
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=your-secret-here

# OAuth Providers
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
NAVER_CLIENT_ID=
NAVER_CLIENT_SECRET=
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=
```

## Documentation

- **Technical Spec**: [TECHSPEC.md - Web Architecture](../docs/ai/TECHSPEC.md#web-architecture-planned---long-term)
- **Roadmap**: [ROADMAP.md - Web Version](../docs/ai/ROADMAP.md#web-version-for-seo--discoverability)
- **Full Plan**: See plan file `cozy-spinning-rainbow.md` for detailed implementation plan

## Implementation Phases

1. Backend SEO support (CORS, controllers)
2. Next.js project setup with NextAuth
3. Public pages (home, browse, recipe detail, profile)
4. Protected pages (create, edit, variation, log)
5. SEO verification & analytics
6. AWS/GCP deployment
7. App integration (deep links, "Open in App" banners)
