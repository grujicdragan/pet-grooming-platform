export interface SalonInfo {
  displayName: string;
  tagline: string | null;
  description: string | null;
  phone: string | null;
  phoneDisplay: string | null;
  phoneHref: string | null;
  email: string | null;
  address: string;
  addressLine1: string | null;
  city: string | null;
  state: string | null;
  country: string | null;
  timezone: string | null;
  mapsSearchUrl: string | null;
  mapEmbedUrl: string | null;
}

export interface BrandingInfo {
  primaryColor: string | null;
  secondaryColor: string | null;
  accentColor: string | null;
  logoUrl: string | null;
  coverImageUrl: string | null;
  fontFamily: string | null;
}

export interface SocialLink {
  platform: string;
  url: string;
  handle: string | null;
}

export interface GroomingService {
  id: string;
  code: string;
  label: string;
  blurb: string;
  minutes: number;
  price: number;
  currency: string;
  icon: string | null;
}

export interface GalleryItem {
  src: string;
  alt: string;
  caption: string;
}

export interface TestimonialItem {
  quote: string;
  name: string;
  pet: string;
  photo: string;
}

export interface SiteSection {
  title: string | null;
  subtitle: string | null;
  body: string | null;
  content: Record<string, unknown>;
  imageUrl: string | null;
  ctaLabel: string | null;
  ctaUrl: string | null;
}

export interface FeatureInfo {
  enabled: boolean;
  config: Record<string, unknown>;
}

export interface TenantSite {
  slug: string;
  name: string;
  salon: SalonInfo;
  branding: BrandingInfo | null;
  location: {
    name: string;
    phone: string | null;
    addressLine1: string;
    city: string;
    state: string | null;
    country: string | null;
  } | null;
  socialLinks: SocialLink[];
  services: GroomingService[];
  gallery: GalleryItem[];
  testimonials: TestimonialItem[];
  sections: Record<string, SiteSection>;
  features: Record<string, FeatureInfo>;
}
