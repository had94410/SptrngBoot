UI asset placement and instructions

This project expects local image assets to improve the storefront appearance. Add your logo and store images as follows:

1) Logo
- Path: src/main/resources/static/images/logo.png
- Recommended sizes: 240x72 (or 120x36 for smaller), PNG or WebP
- Optional retina: logo@2x.png

2) Store images
- Directory: src/main/resources/static/images/stores/
- File names expected by DataInitializer: store01.jpg, store02.jpg, ..., store10.jpg
- The DataInitializer cycles these 10 image filenames when creating 300 sample stores.
- Recommended size: 1280x720 (or 640x360) and web-optimized (JPEG or WebP)

3) Placeholder behaviour
- If a store image is missing, the templates fall back to a remote placeholder image via onerror on the <img> tag.

4) How to add assets locally
- Create the folders if they do not exist:
  src/main/resources/static/images/
  src/main/resources/static/images/stores/
- Put your logo at src/main/resources/static/images/logo.png
- Put store images as store01.jpg..store10.jpg into the stores/ folder.

5) Rebuild / Run
- After adding images, restart the Spring Boot application so the static resources are served.
- Local URL examples:
  - Home: http://localhost:8080
  - Store detail: http://localhost:8080/store/1

6) Optional: use remote images
- If you prefer remote images, update DataInitializer (DataInitializer.java) or the store.imageUrl property in the database to point to full URLs (https://...)

If you want, I can add a small set of sample images (encoded as base64 and written as .png files), but those will significantly bloat the repository. Tell me if you want sample placeholders committed directly.