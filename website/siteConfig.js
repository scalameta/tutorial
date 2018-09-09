// See https://docusaurus.io/docs/site-config.html for all the possible
// site configuration options.

const repoUrl = "https://github.com/scalameta/tutorial";
const gitterUrl = "https://gitter.im/scalameta/scalameta";

const siteConfig = {
  title: "Scalameta",
  tagline: "Library to read, analyze, transform and generate Scala programs",
  url: "https://scalameta.org",
  baseUrl: "/scalameta/",

  // Used for publishing and more
  projectName: "scalameta",
  organizationName: "olafurpg",

  // algolia: {
  //   apiKey: "???",
  //   indexName: "sbt-docusaurus"
  // },

  // For no header links in the top nav bar -> headerLinks: [],
  headerLinks: [
    { doc: "trees/guide", label: "Docs" },
    { doc: "semanticdb/guide", label: "SemanticDB" },
    { href: repoUrl, label: "GitHub", external: true }
  ],

  // If you have users set above, you add it here:
  // users,

  /* path to images for header/footer */
  headerIcon: "img/scalameta.png",
  footerIcon: "img/scalameta.png",
  favicon: "img/favicon.png",

  /* colors for website */
  colors: {
    primaryColor: "#005124",
    secondaryColor: "#181A1F"
  },

  customDocsPath: "docs-out",

  // This copyright info is used in /core/Footer.js and blog rss/atom feeds.
  copyright: `Copyright © ${new Date().getFullYear()} Scalameta`,

  highlight: {
    // Highlight.js theme to use for syntax highlighting in code blocks
    theme: "github"
  },

  /* On page navigation for the current documentation page */
  onPageNav: "separate",

  editUrl: `${repoUrl}/edit/master/docs/`,

  repoUrl,
  gitterUrl
};

module.exports = siteConfig;
